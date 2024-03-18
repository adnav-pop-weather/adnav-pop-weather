import numpy as np
from casex import BallisticDescent2ndOrderDragApproximation, enums, AircraftSpecs
import matplotlib.pyplot as plt


def _simple(resolution):
    fill_value = 1 / resolution**2
    return np.full(shape=(resolution, resolution), fill_value=fill_value)


def compute_pdf(resolution):
    return _simple(resolution)


def _actual():
    # parameters of the aircraft
    aircraft_type = enums.AircraftType.MULTI_ROTOR
    altitude = 3000  # in m
    aircraft_mass = 4753  # in kg
    width = 18.3  # in m
    height = 5.1
    speed = 134.112  # ms^-1

    BDM = BallisticDescent2ndOrderDragApproximation()

    aircraft = AircraftSpecs(aircraft_type, width, aircraft_mass)

    aircraft.set_ballistic_drag_coefficient(np.linspace(0.6, 1.0, 10))
    aircraft.set_ballistic_frontal_area(height * width)
    BDM.set_aircraft(aircraft)

    #  initial_vel_x = np.random.uniform(0, speed + 1, 1000)
    #  initial_vel_y = np.random.uniform(0, speed + 1, 1000)
    #  initial_vel_x = np.linspace(0, speed, 100)
    initial_vel_x = speed

    direction_horizontal = np.linspace(math.pi, 3 / 2 * math.pi, 100, endpoint=False)
    direction_horizontal_cos = np.cos(direction_horizontal)
    direction_horizontal_sin = np.sin(direction_horizontal)

    direction_vertical_cos = np.cos(np.linspace(0, math.pi / 2 - 0.1, dir_ver_resolution, endpoint=False))

    wind_speed = np.linspace(1, wind_max, wind_resolution)
    wind_dir = np.linspace(0 + 0.5, 2 * math.pi + 0.5, wind_resolution, endpoint=False)

    p_vel_x = BDM.compute_ballistic_distance(altitude, initial_vel_x, -1)
    #  p_vel_y = BDM.compute_ballistic_distance(altitude, initial_vel_y, -1)

    distances_x = p_vel_x[0]
    #  distances_y = p_vel_y[0]

    num_bins = 20
    bin_size = np.max((distances_x, distances_y)) + 1 // num_bins

    pdf = np.zeros((num_bins, num_bins))

    # Count occurrences of pairs falling into each bin
    for pair in zip(distances_x, distances_y):
        x_bin = int(pair[0] // bin_size)
        y_bin = int(pair[1] // bin_size)
        pdf[x_bin, y_bin] += 1

    # Normalize counts to obtain probabilities
    # pdf /= np.sum(pdf)

    x = np.linspace(-max(distances_x), max(distances_x), pdf.shape[0])
    y = np.linspace(-max(distances_y), max(distances_y), pdf.shape[1])
    X, Y = np.meshgrid(x, y)

    fig = plt.figure(figsize=(10, 10))
    plt.contourf(X, Y, pdf, cmap='magma')
    plt.show()
