from os import getcwd, remove
# from os.path import exists
import numpy as np
# from scipy.signal import convolve2d
# import math
import matplotlib.pyplot as plt
from time import perf_counter

# from fiona import open as f_open
from shapely import box, voronoi_polygons, MultiPoint, oriented_envelope, Point, LineString
# from shapely.prepared import prep
# from shapely.geometry import shape as shapely_shape

import rasterio
# from rasterio.crs import CRS
# from rasterio import features as ft
# from rasterio.enums import MergeAlg
from rasterio.plot import show
from rasterio.transform import Affine

import geopandas as gpd
from geocube.api.core import make_geocube
# from geocube.vector import vectorize
from functools import partial
from geocube.rasterize import rasterize_image


class Risk_Map_Maker:
    def __init__(self, population_filepath, start_latlng, dest_latlng):
        self._start = start_latlng
        self._dest = dest_latlng
        self._population_filepath = population_filepath
        self._raster_resolution_meters = 275  # CAN BE UPDATED IN THE FUTURE. 280 * 4 = 1120.
        self._raster_transform = None
        self._enclosing_polygon = None

    def _generate_pop_raster(self, gdf, shape, column):
        """
                    Link to solution for creating transform and rasterizing data,
                     and also points to package which can do it for you.
                    https://gis.stackexchange.com/questions/305135/how-to-convert-vector-format-geopackage-shapefile-to-raster-format-geotiff/329764#329764

                """
        start_time = perf_counter()
        bounds = gdf.total_bounds
        transform = rasterio.transform.from_bounds(*bounds, *shape)  # make the transform
        dataset = make_geocube(gdf, ['population'],
                               resolution=(-self._raster_resolution_meters, self._raster_resolution_meters),
                               fill=0.0,
                               rasterize_function=partial(rasterize_image, all_touched=True))  # , resolution=shape
        finish_time = perf_counter()
        print(f'making population geocube: {finish_time - start_time}')
        return dataset, transform

    def _compute_pdf(self, kernel_width):
        fill_value = 1 / (kernel_width ** 2)
        return np.full(shape=(kernel_width, kernel_width), fill_value=fill_value)

    def _generate_expected_population_component(self, population_gdf):
        min_x, min_y, max_x, max_y = self._enclosing_polygon.bounds
        raster_shape = int((max_x - min_x) // self._raster_resolution_meters), \
                       int((max_y - min_y) // self._raster_resolution_meters)

        raster, transform = self._generate_pop_raster(gdf=population_gdf, shape=raster_shape, column='population')
        self._raster_transform = transform

        # NOT IMPLEMENTED. Original was implemented when using rasterio's version of a raster, instead of geocube's.
        # # perform a convolution with the crash distribution over the population_raster to get the expected
        # population #  https://pygis.io/docs/e_raster_window_operations.html #
        # https://datascience.stackexchange.com/questions/91126/understanding-scipy-signal-convolve2d-full
        # -convolution-and-backpropagation-betwe
        # kernel_width_m = 800  # in meters
        # kernel_width = math.ceil(kernel_width_m / self._raster_resolution_meters)
        # print(f'kernel width is: {kernel_width}')
        # # ensure kernel has odd dimensions
        # if kernel_width >= 3:
        #     if kernel_width % 2 == 0:
        #         # even
        #         kernel_width -= 1
        #     kernel = self._compute_pdf(kernel_width)
        #     expected_population = convolve2d(raster.population, kernel, mode='valid')
        # else:
        #     expected_population = raster
        expected_population = raster
        return expected_population

    def _generate_risk_map_raster(self, population_gdf):
        # expected_population_component_path doesn't change throughout the program, so compute once.

        expected_population = self._generate_expected_population_component(population_gdf)

        risk_map = expected_population

        # include drone_area component.
        # NOT IMPLEMENTED in this iteration. We'd need to know the size of each grid element
        #   (hint: either use raster resolution (faster),
        #   or calculate tessellation's features area for each feature (slower, probably)).

        # incorporate crash_frequency component. Based on some research papers we read, they assumed the risk of
        # a critical event causing unrecoverable loss of altitude, or "crashing" was 1 in a 1,000,000.
        risk_map *= 1e-6

        return risk_map

    def get_tessellation(self, spacing_in_m):
        # create points inside enclosing polygon, and turn to multipoint object
        start_time = perf_counter()
        minx, miny, maxx, maxy = self._enclosing_polygon.bounds
        num_points_x = round((maxx - minx) / spacing_in_m)
        num_points_y = round((maxy - miny) / spacing_in_m)
        x = np.linspace(minx, maxx, num_points_x)
        y = np.linspace(miny, maxy, num_points_y)

        x_grid, y_grid = np.meshgrid(x, y)
        multi_point = MultiPoint([(x_i, y_i) for x_i, y_i in zip(x_grid.ravel(), y_grid.ravel())])

        tessellation = gpd.GeoDataFrame(
            {'geometry': voronoi_polygons(multi_point, extend_to=self._enclosing_polygon).geoms})
        tessellation.set_crs(epsg=3857, inplace=True)
        # clip to the area of interest. Voronoi tessellations create 'infinite regions' that we need to fix. Ideally,
        #   that's what the extend_to= argument does, but it doesn't work, so we have to clip to our polygon.
        #   Other notes: since the polygon is a box-like feature, a faster clipping algorithm is used.
        tessellation = gpd.clip(tessellation, oriented_envelope(self._enclosing_polygon))

        # Create an index_column, for use in extracting risk data from the raster to the tessellation.
        #   Data type has to be float32 because geocube's make_geocube() method only supports a few types of integers,
        #   which are too small for creating unique indices. Therefore, we use float32.
        tessellation['index_column'] = tessellation.reset_index().index.astype('float32')
        finish_time = perf_counter()
        print(f'making tessellation: {finish_time - start_time}')
        print(f'created {tessellation.shape[0]} features')
        return tessellation

    def get_risk_map_vector(self, tessellation, raster):
        # try using geocube to combine the tessellated grid and then use the index to create shapes.
        start = perf_counter()
        # start_geocube = perf_counter()
        # Make the gridded_dataset like our raster, so they align, and we can line pixel values up to extract risk.
        gridded_dataset = make_geocube(tessellation,
                                       measurements=['index_column'],
                                       like=raster,
                                       rasterize_function=partial(rasterize_image))
        gridded_dataset['population'] = raster.population  # We can do this illegal move since they're aligned.
        gridded_dataset['population'] = gridded_dataset['population'].fillna(0.0)  # Fill places with no risk as 0.0.
        # end_geocube = perf_counter()
        # print(f'making the gridded dataset and adding population: {end_geocube - start_geocube}')

        # Since the gridded_dataset has risk and associated indices, and we know the index of each polygon
        #   in our tessellation, we can group the risk by the index column, and compute the mean.
        #   Note: converting to pandas dataframe and grouping was much,
        #       much faster than doing through geocube's methods.
        grouped_population = gridded_dataset \
            .to_dataframe() \
            .groupby('index_column', as_index=False, dropna=True)['population'].mean()

        # when assigning the columns to be the same it assigns by index and not left-right as you'd expect. So for
        # example, index 98, and index_col=98 has pop value=0.00007, when added to new_gdf it gets added to
        # new_gdf.index=98, index_column=0, offline_risk sets to 0.00007.

        # To work around above_mentioned problem, we sort the two dataframes, and reset their indices.
        # Now they're aligned with each other, and we can append the column. This is very fast.
        tessellation = tessellation.sort_values(by=['index_column']).reset_index()
        grouped_population = grouped_population.sort_values(by=['index_column']).reset_index()
        tessellation['offline_risk'] = grouped_population['population']

        finish = perf_counter()
        print(f'making vector: {finish - start}')
        # set to one because when we multiply by presence of weather we will multiply by 1000.
        tessellation['weather_mult']: gpd.GeoDataFrame = 1
        # change crs to epsg:4326 "lat/lon" for better integration with XPlane. Previous code base was written with
        #   long/lat in mind, and population data was in meters, so convert for convenience.
        tessellation = tessellation.to_crs(epsg=4326)
        tessellation = tessellation.drop(columns=['index', 'index_column'])  # Keeping these columns wastes space.
        # will overwrite existing file.
        tessellation.to_file('offline_risk.gpkg', layer='offline_risk', driver='GPKG', engine='pyogrio')
        return tessellation

    def get_risk_map(self, population_gdf):
        risk_map_raster = self._generate_risk_map_raster(population_gdf)
        return risk_map_raster

    def get_population_data(self):
        start_pop = perf_counter()

        start_latlng = Point(self._start[::-1])
        dest_latlng = Point(self._dest[::-1])
        straight_line = LineString([start_latlng, dest_latlng])
        midpoint_latlng = straight_line.interpolate(0.5, normalized=True)
        poly = midpoint_latlng.buffer((straight_line.length * 1.10) / 2)

        masking_series = gpd.GeoSeries([poly], crs='EPSG:4326').to_crs(crs="EPSG:3857")
        masking_poly = oriented_envelope(masking_series[0])

        # pyogrio is much faster at loading data than fiona, use_arrow loads data in an array (we think)
        #   but it speeds things up even faster. Unfortunately, you can't use a fancy bbox with pyogrio (yet),
        #   so you're limited to boxes, like envelope, oriented_envelope, box, rect, etc.
        population_gdf = gpd.read_file(self._population_filepath,
                                       bbox=masking_poly,
                                       engine='pyogrio',
                                       use_arrow=True)
        population_gdf['population'] = population_gdf['population'].fillna(value={'population': 0.0}).astype(int)
        self._enclosing_polygon = box(*population_gdf.total_bounds.tolist())
        finish_pop = perf_counter()
        print(f'loading population data took: {finish_pop - start_pop}')
        print(f'loaded {population_gdf.shape[0]} features')
        # population_gdf.plot(column='population', cmap='jet')
        # plt.show()
        return population_gdf

    def show_risk_map_raster(self):
        with rasterio.open(getcwd() + "\\risk_map_raster.tif", 'r') as src:
            show(src, cmap='jet')

    def show_maps(self, original_data, raster, grid, extracted_to_grid):
        cmap = 'jet'
        fig, axs = plt.subplots(2, 2)
        original_data.plot(ax=axs[0][0], column='population', cmap=cmap)
        # show(raster, ax=axs[1], cmap=cmap)
        # if extracted_to_grid is not None:
        #     extracted_to_grid.plot(ax=axs[3], cmap=cmap)
        # plt.tight_layout()
        # plt.show()
        raster.population.plot(ax=axs[0][1], cmap=cmap)
        grid.plot(ax=axs[1][0], facecolor='white', edgecolor='red', linewidth=0.1)
        extracted_to_grid.plot(ax=axs[1][1], column='offline_risk', cmap=cmap)
        plt.show()
