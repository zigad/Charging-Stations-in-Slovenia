# Charging Stations in Slovenia

This repository contains a Java program that fetches a list of charging stations available in Slovenia from various providers and creates a new file with newly added charging stations. The program is designed to run on a schedule via cron job on my personal
server, checking for any changes in the number of stations every few hours. If there are any new or removed stations, a new file is generated and pushed to the correct directory and then of course uploaded to this repository so users have an insight of how
often new charging stations are added in Slovenia by each provider.

Currently, the program checks for updates from the following charging providers:

- [Gremo Na Elektriko](https://www.gremonaelektriko.si)
- [Petrol / One Charge](https://www.petrol.si/mobilnost/zasebni-uporabniki/javne-elektricne-polnilnice)
- [Moon Charge](https://www.vrhunskaemobilnost.si/moon-charge/)
- [Avant2Go](https://avant2go.si) (This charging stations are private and available only for Avant2Go customers!)
- [MOL Plungee](https://molplugee.si/si) (Currently not implemented, still using Google Scripts to fetch new data)

In addition, an email notification is sent out whenever a new station is added to the list. This is used for me so when a new station is added I can publish it to PlugShare as soon as possible
This repository is intended to provide an up-to-date and accurate list of newly added charging stations for electric vehicle owners in Slovenia. The program can also be easily modified to include additional providers or to be used in other countries.

Overall, the Charging Stations in Slovenia repository is a valuable resource for electric vehicle owners in Slovenia, providing a convenient and easy way to access the latest information about what charging stations were recently added in the country.

### How it works

The program fetches the list of charging stations from each implemented provider from their API. Saves it to POJO, and then compares IDs from the ones that are stored in `currentInfoPerProvider.json` file (Why use a database if you have json 🤣). if a change is detected, a new file is generated and stored in the corresponding folder with a specific filename: `Provider_timestamp.json`. The timestamp is in the following format: `yyyy.MM.dd@HH.mm.ss`. When a new file is generated and `currentInfoPerProvider.json` is updated program will commit changes to this repository.

### How can you help?

If you wish you can optimize the code, check for bugs and add new providers, open an issue, and we'll discuss it. That's why it's open source.

### Configuration

To make sure this program runs on your computer, make sure you have `currentInfoPerProvider.json` file, and you configure `configuration.properties`. You can look at the example file in this repo. Otherwise, there should be no problems with running this. If you want to generate `.jar` file, use maven command `mvn package`, and you'll have `Charging-Stations-in-Slovenia.jar` file in the target folder

Disclaimer: _This README was definitely not written with the help of ChatGPT._