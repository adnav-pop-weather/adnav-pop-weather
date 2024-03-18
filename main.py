import os
import math as mth

import geopandas as gpd
import fiona
from shapely.geometry import box
import numpy as np
import pygeos
import matplotlib.pyplot as plt
import rasterio
from rasterio import features as ft
from rasterio.enums import MergeAlg
from rasterio.plot import show
from rasterio.transform import Affine
from scipy.signal import convolve2d

from Ballistic_Descent_Model import compute_pdf
from Risk_Map_Test import run_test


def get_parameters(data_path):
    # returns the parameters needed for the following code
    # open the file and get the bounds
    with fiona.open(data_path, 'r') as f:
        # get the bounding box
        bounding_tuple = f.bounds

    bounding_box = box(*bounding_tuple).bounds

    # set the min_x, max_x, min_y, max_y as a quarter of the bounds for testing
    min_x = bounding_box[0]
    max_x = bounding_box[2]
    min_y = bounding_box[1]
    max_y = bounding_box[3]

    width = max_x - min_x
    height = max_y - min_y

    width *= 0.25
    height *= 0.25

    new_max_x = min_x + width
    new_max_y = min_y + height

    # no longer using resolution from here, instead calculate from dataset?
    # resolution = 200  # test resolution as 50.

    return min_x, new_max_x, min_y, new_max_y  # , resolution


"""
    Attribution: Code inspired by https://dmnfarrell.github.io/plotting/geopandas-grids.
    Returns a grid of squares over the area.
"""


def create_grid(min_x, min_y, max_x, max_y, num_cells, crs):
    x_points = np.linspace(min_x, max_x, num_cells)
    y_points = np.linspace(min_y, max_y, num_cells)

    """
        Meshgrid code inspired by:
         https://gis.stackexchange.com/questions/414617/creating-polygon-grid-from-point-grid-using-geopandas
         
        Idea is to use a meshgrid and use the array indexing (constant time) to more quickly make shapes than using
         for loops.
    """

    xx, yy = np.meshgrid(x_points, y_points, indexing="ij")  # make the meshgrid

    xx_one = xx.ravel()
    yy_one = yy.ravel()
    points = pygeos.creation.points(xx_one, yy_one)  # get the points to use for the creation of squares.

    points = pygeos.make_valid(points)

    points_shapely = pygeos.to_shapely(points)

    # make an intermediary geodataframe to create the actual lat/lon coordinates, for use later
    points_gdf = gpd.GeoDataFrame(geometry=points_shapely)

    # get the coordinates of the points.
    coords = pygeos.get_coordinates(pygeos.from_shapely(points_gdf.geometry))

    n_row = num_cells
    n_col = num_cells
    n = (n_row - 1) * (n_col - 1)
    n_vertex = (n_row + 1) * (n_col + 1)

    x = coords[:, 0]
    y = coords[:, 1]

    order = np.lexsort((x, y))
    x = x[order].reshape((n_row, n_col))
    y = y[order].reshape((n_row, n_col))

    # Set up the indexers.
    left = lower = slice(None, -1)
    upper = right = slice(1, None)
    corners = [
        [lower, left],
        [lower, right],
        [upper, right],
        [upper, left],
    ]

    xy = np.empty((n, 4, 2))

    # could not broadcast input array from shape (576,) into shape (625,)
    for i, (rows, cols) in enumerate(corners):
        xy[:, i, 0] = x[rows, cols].ravel()
        xy[:, i, 1] = y[rows, cols].ravel()

    """# find a more efficient way to make features. Maybe try exploring meshgrid,
    # and using values from there to get coords.
    for x0 in x_points:
        for y0 in y_points:
            x1 = x0 - cell_size
            y1 = y0 + cell_size
            poly = box(x0, y0, x1, y1)
            grid_cells.append(poly)
    """

    mesh_geometry = pygeos.to_shapely(pygeos.creation.polygons(xy))
    grid = gpd.GeoDataFrame(geometry=mesh_geometry, crs=crs)
    return grid


def plot_population_overlay(gdf, grid):
    fig, ax = plt.subplots(1, 1, figsize=(12, 6))

    gdf.plot(ax=ax, column='population', cmap='jet')
    grid.plot(ax=ax, facecolor='none', edgecolor='white', linewidth=0.5)
    plt.show()


def plot_population(gdf, ax=None):
    if ax is None:
        fig, ax = plt.subplots(1, 1, figsize=(12, 6))

    gdf.plot(ax=ax, column='population', cmap='jet')
    plt.show()


def plot_3d(raster, shape):
    fig, ax = plt.subplots(1, 1, subplot_kw={"projection": "3d"})

    x = np.linspace(0, 1000, shape[0])
    y = np.linspace(0, 1000, shape[1])

    X, Y = np.meshgrid(x, y)

    surf = ax.plot_surface(X, Y, raster, cmap='jet', linewidth=0, )
    # surf = ax.scatter(X, Y, raster, c=raster, cmap='magma', marker='o')
    ax.set_xlabel('$x$')
    ax.set_ylabel('$y$')
    ax.set_zlabel('$population$')
    ax.set_title('Pop Data in Raster')
    plt.show()


def do_population_stuff():
    # get the geodataframe data
    gdf_path = os.getcwd() + '\\population.gpkg'

    min_x, max_x, min_y, max_y = get_parameters(gdf_path)  # get the parameters of the map [removed , resolution]
    print(f'done retrieving params\nStart retrieving features...')

    bbox = box(min_x, min_y, max_x, max_y)

    # POINT (-11135132.643 3717941.711) invalid point?
    gdf = gpd.read_file(gdf_path, bbox=bbox)
    #  print(f'number of features: {gdf.shape[0]}\n resolution should be {resolution}')
    print(f'done reading features')

    # print(f'creating grid')
    # square_grid = create_grid(min_x=min_x, min_y=min_y, max_x=max_x, max_y=max_y, num_cells=resolution, crs=gdf.crs)
    # print(f'done creating grid')

    # convert the vector data to raster data
    """
        Link to solution for creating transform and rasterizing data,
         and also points to package which can do it for you.
        https://gis.stackexchange.com/questions/305135/how-to-convert-vector-format-geopackage-shapefile-to-raster-format-geotiff/329764#329764

    """
    bounds = gdf.total_bounds
    shape = 1000, 1000  # how large we want the array to be. WILL NEED TO CHANGE BASED ON WIDTH OF DATA AND RESOLUTION.
    transform = rasterio.transform.from_bounds(*bounds, *shape)  # make the transform

    raster = ft.rasterize([(geom, pop) for geom, pop in zip(gdf['geometry'], gdf['population'])],
                          out_shape=shape,
                          fill=0,
                          transform=transform,
                          all_touched=True,
                          merge_alg=MergeAlg.replace,
                          dtype=np.double)  # Rasterize the data

    # plot the data
    fig, ax = plt.subplots(1, 2, figsize=(10, 10))
    ax[1].set_title('Population Data Converted to Raster')
    ax[0].set_title('Vector Population Data')
    show(raster, ax=ax[1], cmap='jet')
    plot_population(gdf, ax=ax[0])
    plot_3d(raster, shape)  # also plot in 3d to satisfy people.

    # Generate the crash distribution
    # Use the width of the vector data and the shape of the raster
    #  to get the resolution of each element in the distribution matrix.
    """
        resolution = width_of_vec_data // raster_shape[0]
        and then, for argument's sake, assume distribution says plane crashes at most 300m from the point of event.
        then, shape_of_distribution = width of 99% crash prob // resolution
        Also remember resolution is measured in feet [feet], and crash prob will be in meters [m].
    """
    kernel_width = 5  # kernel MUST have ODD shape: 1..3..5... Cannot work with EVEN 2..4..6...
    kernel = compute_pdf(kernel_width)

    # perform a convolution with the crash distribution over the population_raster to get the expected population
    #  https://pygis.io/docs/e_raster_window_operations.html
    #  https://datascience.stackexchange.com/questions/91126/understanding-scipy-signal-convolve2d-full-convolution-and-backpropagation-betwe
    # scipy?
    expected_population = convolve2d(raster, kernel, mode='valid')

    # plot the raster and the convoluted values
    fig, ax = plt.subplots(1, 2, figsize=(10, 10))
    show(expected_population, ax=ax[1], cmap='jet')
    show(raster, ax=ax[0], cmap='jet')
    ax[1].set_title('Expected Population after applying Crash Distribution\n'
                    'Current dist: Average Neighborhood\nIn Progress dist: Ballistic Descent Model')
    ax[0].set_title('Raster Values')
    plt.show()

    # apply the frequency of crash events occurring.

    # then, multiply by the Area_Exposed to the crash

    #  ---- pathfinding strategy -------
    # perform gradient descent pathfinding for the hell of it
    print(f'performing gradient descent')
    # now turn the values in the risk matrix into a graph
    # rasterio to vector

    # perform A-star search

    # smooth the path (optional)

    # send the path to the controller.


if __name__ == '__main__':
    #  run_test()
    do_population_stuff()
