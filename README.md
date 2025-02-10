![Secure-Storage-Manager](Resources/Splash.png)

# 🔐 Secure-Storage-Manager 🔒

*A standalone Java based cryptographic file manager built using Maven ,Swing ,SQLite.*

## Features  
- **AES-256 encryption.**
- **Custom AES CBC/ECB decryption**
- **SHA-256 & SHA-512 checksum for file integrity.** 
- **Embedded database for storing keys and encrypted files.**
- **Secure login system for users.**
- **Admin console to handle database.**

## Installation
- Windows : Go to release tab and install and run ``Secure-Storage-Installer_v1.1.0`` all dependencies are included.

## Build 
- Maven : ``mvn clean install``
- JAR   : ``jar cfm SecureStorageManager.jar MANIFEST.MF -C target\classes\ . ``
- jdeps : ``java.base , java.desktop , java.sql``

## Execute 
- Maven : ``java -jar target/Secure_Storage_Manager-1.0-SNAPSHOT.jar``
- JAR   : ``java -jar SecureStorageManager.jar``




