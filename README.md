# Where Did I Park?

Simple Android application for managing parked cars and their location. It allows users to save the current GPS location of their car and later find it based on the saved location. The application allows you to add cars, display them and store information about their parking.

## Functions
- Add as many cars with name and picture as you have.
- Save and then display the saved location of all your cars.
- Explore the parking history.
- Change your cars picture, delete parking history or delete whole car record.

## How to use
1. **Adding a car**: After launching the application, click the **Add new car** button. Enter the name of the car and upload a picture.
2. **Save your car location**: After you parked your car, click the **Save Location** button to save the current GPS position of your car.
3. **Find your car**: When you want to find your car, click **Find car** button to show current location of your car on the map.
3. **Explore parking history**: You can view the parking history and explore saved car locations.

## Permissions
The app requires following permissions:
- **ACCESS_FINE_LOCATION**: For precise location using GPS.
- **ACCESS_COARSE_LOCATION**: For approximate location detection.
- **READ_MEDIA_IMAGES**: To allow reading car images saved by the user.
- **INTERNET**: To access online services enabling the proper functionality of the Google Maps API.
- **ACCESS_NETWORK_STATE**: To check the network status.

## Installation
1. Clone this repository to your local PC:
   git clone https://github.com/Marty9406/WhereDidIParked.git
2. Open the project in Android Studio.
3. Replace the placeholder **paste_your_API_KEY_here** in the AndroidManifest.xml file with your Google Maps API key.
4. Run the application on the emulator or your own device.

## Used technology
- SQLite - For storing information about cars and their parking locations.
- Google Maps API and Location API - For location and map display.
- ShaderPreferences - For loading the last viewed car.
- ImagePicker - For choosing image for each of your car.

## Purpose
This is a university project for Design of Applications for Mobile Devices II subject. However, I will be using this application in my free time to easily find my car in a densely populated housing estate, where I park it in a different spot after each work shift.
