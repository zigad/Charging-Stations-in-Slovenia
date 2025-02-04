-- Delete providers table if exists
DROP TABLE IF EXISTS providers CASCADE;

-- Create providers table
CREATE TABLE providers
(
    id      INTEGER PRIMARY KEY,
    name    TEXT NOT NULL
);

-- Insert Providers
INSERT INTO providers (id, name)
VALUES  (1, 'GremoNaElektriko'),
        (2, 'Petrol'),
        (3, 'MoonCharge'),
        (4, 'eFrend'),
        (5, 'MegaTel'),
        (6, 'Avant2Go'),
        (7, 'Implera');

-- Insert initial data into charging_stations table
INSERT INTO charging_stations (station_id, provider, friendly_name, address, location)
VALUES  ('2611995', 2, 'BTC - Tržnica', 'Šmartinska cesta 152', null),
        ('219758', 1, '1', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 2, '2', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 3, '3', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 4, '4', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 5, '5', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 6, '6', 'Obrežna ulica 170', '46.5667880,15.6065760'),
        ('219758', 7, '7', 'Obrežna ulica 170', '46.5667880,15.6065760');

