# 🔐 Secure-Storage-Manager 🔒

*A standalone Java based cryptographic file manager built using Maven ,Swing ,SQLite.*

## Features  
- **AES-256 encryption.**
- **SHA-256 & SHA-512 checksum for file integrity.** 
- **Embedded database for storing keys and encrypted files.**
- **Secure login system for users.**
- **Admin console to handle database.**

## Build 
- Maven : ``mvn clean install``
- JAR   : ``jar cfm SecureStorageManager.jar MANIFEST.MF -C target\classes\ . ``
- jdeps : ``java.base , java.desktop , java.sql``

## Execute 
- Maven : ``java -jar target/Secure_Storage_Manager-1.0-SNAPSHOT.jar``
- JAR   : ``java -jar SecureStorageManager.jar``

## Installation
- Windows : Go to release tab and then install and run ``Secure-Storage-Installer_v1.0.0`` all dependencies are included.


