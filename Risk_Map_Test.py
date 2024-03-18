import numpy as np
import matplotlib.pyplot as plt
import math as mth
import random


def create_grid(min_x, max_x, min_y, max_y, resolution=25):
    # creates the grid based on the resolution and returns a grid with resolution x resolution points
    x = np.linspace(min_x, max_x, resolution)
    y = np.linspace(min_y, max_y, resolution)
    return np.meshgrid(x, y)


def create_goal_obstacles(min_x, max_x, min_y, max_y, min_obstacles, max_obstacles):
    goal = np.array([random.randint(min_x, max_x), random.randint(min_y, max_y)])
    obstacles = np.array([np.array([random.randint(min_x, max_x), random.randint(min_y, max_y)]) for _ in
                          range(random.randint(min_obstacles, max_obstacles))])
    return goal, obstacles


def dist(x1, y1, x2, y2):
    return np.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


def attraction(X, Y, goal):
    return dist(X, Y, goal[0], goal[1])


def repulsion(X, Y, obstacles):
    R = 2
    mats = []
    for obstacle in obstacles:
        d = dist(X, Y, obstacle[0], obstacle[1])
        mats.append(np.where(d >= R, 0, 2 / (d + 0.0001)))
    result = np.zeros(X.shape)
    for mat in mats:
        result += mat
    return result


def cost(X, Y, goal, obstacles):
    # sum of the attraction towards the goal and the repulsion from the obstacles.
    return attraction(X, Y, goal) + repulsion(X, Y, obstacles)


def plot_3d(X, Y, Z):
    fig, ax = plt.subplots(1, 1, subplot_kw={"projection": "3d"})

    surf = ax.plot_surface(X, Y, Z, cmap='jet', linewidth=0, )
    # surf = ax.scatter(X, Y, Z, c=Z, cmap='jet', marker='o')
    ax.set_xlabel('$x$')
    ax.set_ylabel('$y$')
    ax.set_title('Cost Function')
    plt.show()


def get_parameters():
    min_x = 0
    max_x = 10
    min_y = 0
    max_y = 10
    resolution = 25
    min_obstacles = 1
    max_obstacles = 5
    return min_x, max_x, min_y, max_y, resolution, min_obstacles, max_obstacles


def run_test():
    # set the parameters of the grid.
    min_x, max_x, min_y, max_y, resolution, min_obstacles, max_obstacles = get_parameters()

    X, Y = create_grid(min_x, max_x, min_y, max_y, resolution)  # get the matrices representing the grid.

    # create the obstacles
    goal, obstacles = create_goal_obstacles(min_x, max_x, min_y, max_y, min_obstacles, max_obstacles)

    Z = cost(X, Y, goal, obstacles)

    # print goal and obstacle locations
    print(f'goal:\n{goal}\nobstacles:\n{obstacles}')

    plot_3d(X, Y, Z)
