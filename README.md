# Homework

This is a typical list/details Android interview homework problem.  

## Project Overview

The app fetches a list of Github repositories from the [Github API](https://docs.github.com/en/rest) and displays them in a list. Paging is supported by swiping up on the list page.  When a repository is tapped, the app navigates to a detailed view showing additional information about the selected repository.  

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVI
- **Navigation:** Compose Navigation
- **Networking:** Retrofit
- **Dependency Injection:** Dagger Hilt
- **Testing:** MockK, Espresso
- **Other:** Coil, Lottie, Timber
- **Build Scripts:** Kotlin DSL
   
## Implementation Decisions

**Single Module:** This app uses a single module due to its small project size. For larger and more complex projects, a modular architecture—such as separating domain, data, and feature modules—would be a cleaner and more scalable approach.

**Clean Architecture:** While separating the data and UI layers with a domain layer may seem excessive for this project, it serves as an intentional demonstration of clean architecture principles.  

**MVI/MVVM:** I decided to use MVI because I thought it would be nice to use in order to manage states and events on the explore page.

**Paging:** I implemented a custom paging solution instead of using the Paging 3 library because each item only handles a small amount of data, and in my experience the Android Paging libraries can be difficult to squeeze into a true clean architecture.

**Assumptions:** I've locked the app to portrait orientation since my last two companies have used this mode.

**Future Improvements:** Additional unit tests and screenshot tests could enhance the project.  

## Clean Architecture

This project adheres to clean architecture principles to ensure clear separation of concerns. The data and UI layers are
fully decoupled, with the domain layer serving as the intermediary.  

<img width="1316" alt="CleanArchitecture" src="https://github.com/user-attachments/assets/8ff5fbff-b7b0-481e-b424-396d7e9d3353" />


## Setup

### Prerequisites
- Android Studio Ladybug or newer

### Steps to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/lmorda/android-interview-problem-mvi.git
   ```
2. Open the project in Android Studio
3. Sync Gradle
4. Run the app on an emulator or a physical device

## Asset Acknowledgement

Thanks for the app icons!

https://www.flaticon.com/authors/agung-rama

https://www.flaticon.com/authors/dave-gandy