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

### Assumptions
- The application only models elements specifically mentioned in the requirements.
  For instance, coworking spaces holding the devices are _not_ represented.
- All configuration values are mandatory.
- A device id is a semantic value (like a serial number), assigned by the application client, not a technical surrogate key provided by the data store.
- Device passwords can be stored and retrieved in plain text. 
  In a real-world application, they would be either hashed and salted or encrypted at rest,
  depending on the purpose for which they are stored.
  In both cases, they would be stored in a separate part of the database with stricter access control
  and also have dedicated, additionally protected endpoints. 
- Access control to the API and other security concerns are outside the scope.

### Design Decisions
The solution follows the hexagonal architecture concepts.
That is, the periphery components (persistence and API) reference the application logic,
while the logic is self-contained.

The logic in this CRU(D) application is trivial.
Nevertheless, this approach was chosen in order to present the general development style.

### Data model
Every device type, such as `Computer` is represented by its own class, extending the abstract `Device` class.

### Persistence
For the data store, two types of databases would have been suitable:
- an relational database like PostgreSQL
- a document store like MongoDB

Both would allow all four required operations to be performant at scale.
As for ACID guarantees, they are offered by both, traditional SQL databases 
and latest versions of modern document stores.

The device configurations do not share any relationship with each other, as of the current requirements,
hence a document store would serve well.

However, it might be a safer choice to remain flexible for future requirements. 
An example could be the ability to select those devices that have a certain type or belong to a certain set of coworking spaces,
like _all computers in German offices_. 
Such relationships are more easily represented in a relational database.
Therefore, for this application, PostgreSQL has been chosen as the persistence solution

### Data model and entity mapping
The application's persistence layer leverages JPA/Hibernate as the ORM, together with Spring Data.

To represent the data model in the relational database,
out of JPA's four common inheritance mapping strategies, _table per class_ has been chosen.
Each concrete device type, such as _computer_, has its own database table,
avoiding nullable columns for values only available for that device type.
No _join_ will be required for the queries, 
while the polymorphic retrieval of all devices is supported through a _union_ query.
The inheritance hierarchy of the entities at the persistence layer mirrors the data model.
The device id is used as the primary key.

### API layer
The application provides a REST API to its clients with the following endpoints:
- `POST /devices` creates a new configuration for a device
- `PUT /devices/<device id>` updates a device configuration by the device id
- `GET /devices/<device id>` retrieves a single device configuration by the device id
- `GET /devices` lists the configurations of all devices

