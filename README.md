# Spring Boot Kotlin Demo

## Domain
An office rental company owns multiple coworking spaces 
providing their guests with devices like computers, displays, coffee machines etc.
Each device has an id, a type and a name.
Additionally, a device can have configuration properties specific to its type.
A computer, for instance, can have a username, a password and an ip address.

## Application
The application is a backend service that stores configurations of differant devices.
It provides an API endpoint that allows clients to:
- create a configuration for a new device
- update the configuration of an existing device
- retrieve the configuration of a device by its id 
- list all device configurations
