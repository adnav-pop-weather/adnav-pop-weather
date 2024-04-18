from os import getcwd
import sys

from Risk_Map_Maker import Risk_Map_Maker as risk_map_maker

"""
    generate_risk_vector_format(filepath) -> None
        Returns a grid of polygons that represent the risk of overflight by the drone for the area they enclose.
        
        Risk is calculated the following way:
        risk(x,y) [population * h^-1] = crash_frequency [h^-1] *
                                        drone_cross_sectional_area [m^2] *
                                        expected_population [population * m^-2]
                                        
        Steps in the function:
        1. Check if expected_population_component already exists
        |-> 1a. If not, create it.
        2. Include expected_population_component to risk_map_raster
        2. Include drone_cross_sectional_area component to risk_map_raster
        3. Include crash_frequency component to risk_map_raster
        |-> 3a. Crash frequency calculated for the non-weather component with assumed failure rate
                of 1 * 10^-6 (notation here: 1e-6)
        4. Create a polygonal_grid over the area.
        5. Extract the values from the risk_map_raster into the polygonal_grid, creating the risk_map_vector_grid.
        6. Save the risk_map_vector_grid.
"""


def generate_risk_vector_format(population_filepath, start_loc, dest_loc):
    start_latlng = start_loc
    dest_latlng = dest_loc
    # load in as lat/long. Need to convert from lat/long(y,x) to meters(x,y)
    dallas_to_wichita_start_latlng = [32.893895180625904, -97.0509447245488]
    dallas_to_wichita_dest_latlng = [33.86057477654421, -98.49042166496733]
    dallas_to_OKC_start_latlng = [32.847865944795615, -96.8464867827233]
    dallas_to_OKC_dest_latlng = [35.392241610901344, -97.58920932238786]

    map_maker = risk_map_maker(population_filepath=population_filepath,
                               start_latlng=start_latlng,
                               dest_latlng=dest_latlng)

    # get population data
    population_gdf = map_maker.get_population_data()

    # create raster risk map
    risk_map_raster = map_maker.get_risk_map(population_gdf)
    del population_gdf

    # Spacing used to be important, I don't think it does in this implementation. Previously, having spacing other than
    #   a multiple of 4 of the raster resolution produced risk_map_vector_grid with elements of varying sizes,
    #   don't think it's a concern anymore.
    polygonal_grid = map_maker.get_tessellation(spacing_in_m=1100)

    # extract values from risk_map_raster into polygonal_grid, creating risk_map_vector_grid.
    risk_map_vector_grid = map_maker.get_risk_map_vector(tessellation=polygonal_grid, raster=risk_map_raster)


if __name__ == '__main__':
    # example arguments: "32.893895180625904", "-97.0509447245488", "33.86057477654421", "-98.49042166496733"
    start_loc = [float(sys.argv[1]), float(sys.argv[2])]
    dest_loc = [float(sys.argv[3]), float(sys.argv[4])]
    filepath = getcwd() + '\\data\\Kontur_Population_US_20231029.gpkg'  # filepath to the data source
    generate_risk_vector_format(population_filepath=filepath, start_loc=start_loc, dest_loc=dest_loc)  # generate risk map in vector format
