# Polnilnice

This repository contains a JSON list of chargings stations available in Slovenia.

Few times a day, script checks if the number of stations is the same as last time it checked, and if the old number doesn't match, a new file will be generated and pushed to the correct directory.

Currently, this repository checks the number of stations for the following charging providers:

- Gremo Na Elektriko
- Petrol / One Charge
- Moon Charge
- Avant2Go
- MOL Plungee


If a new station is added an email is sent.
