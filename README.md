![Secure-Storage-Manager](Resources/Splash.png)

# 🔐 Secure-Storage-Manager 🔒

**A standalone Java based cryptographic file manager built using Maven ,Swing ,SQLite for streamlined symmetric AES encryption and decryption with SHA checksums for file data integrity and secure file and key storage.**

## Features  
- **AES-256 encryption.**
- **Custom AES CBC/ECB decryption**
- **SHA-256 & SHA-512 checksum for file integrity.** 
- **Embedded database for storing keys and encrypted files.**
- **Secure login system for users.**
- **Admin console to handle database.**

## Installation
- Windows : Go to release tab and install and run ``Secure-Storage-Installer_v1.1.0`` all dependencies are included.
- Linux : Clone the repository then use following scripts to build and run the project 
   - ``bash build.sh`` 
   - ``bash run.sh``

## Build 
- Maven : ``mvn clean install``
- Bash  : ``bash build.sh`` 
- jdeps : ``java.base , java.desktop , java.sql``

## Execute 
- Maven : ``java -jar target/Secure_Storage_Manager-1.0-SNAPSHOT.jar``
- Bash  : ``bash run.sh``




