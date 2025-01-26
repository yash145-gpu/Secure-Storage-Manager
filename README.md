# 🔐 Secure-Storage-Manager 🔒

*A standalone Java based cryptographic file manager built using Maven ,Swing ,SQLite.*

## Features : 
- **AES-256 encryption.**
- **SHA-256 & SHA-512 checksum for file integrity.** 
- **Embedded database for storing keys and encrypted files.**
- **Secure login system for users.**
- **Admin console to handle database.**

## Build :
- Maven : ``mvn clean install``
- JAR   : ``jar cfm SecureStorageManager.jar MANIFEST.MF -C target\classes\ . ``
- Execute JAR   : ``java -jar SecureStorageManager.jar``
- jdeps : ``java.base , java.desktop , java.sql``



