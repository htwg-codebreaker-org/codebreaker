[![Coverage Status](https://coveralls.io/repos/github/htwg-codebreaker-org/codebreaker/badge.svg?branch=main)](https://coveralls.io/github/htwg-codebreaker-org/codebreaker?branch=main)
# Codebreaker: Cyberkrieg
Unsere Grundlegende Idee lässt sich in folgendem Beitrag finden und wird dort erklärt.
https://iamnico42.github.io/portfolio/studium/Codebreaker%20-%20Cyberkrieg%28Scala%29/Grundlegende%20Idee/



## Build the Docker Image

From the project root directory:
`docker build -t codebreaker:v1 .`

This creates a Docker image containing:
- Java 21
- Scala 3.6.4
- SBT
- All required dependencies

---

## Run the Application (Console / TUI Mode)

This works **on all systems without any additional setup**:
`docker run -ti codebreaker:v1`
If no graphical display is available, the application automatically runs in **text-based mode**.

---

## Run the Application with GUI (JavaFX)

Docker containers do not provide a graphical display by default.  
To run the JavaFX GUI, an **external X11 server** is required on the host system.

### Windows (recommended setup from lecture)

1. Install **VcXsrv (XLaunch)**  
    [https://sourceforge.net/projects/vcxsrv/](https://sourceforge.net/projects/vcxsrv/)
    
2. Start **XLaunch** with the following settings:
    - Multiple windows
    - Display number: `0`
    - Start no client
    - ✅Check on Disable access control and ❌disable the other ones
        
3. Start the container with GUI support:
    

`docker run -e DISPLAY=host.docker.internal:0 -ti codebreaker:v1`

If u wanna close the x11 Server then just type because its running in the background after launching it with the settings above
`taskkill /IM vcxsrv.exe /F`