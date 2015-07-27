package eu.phiwa.dragontravel.core.filehandlers;

import eu.phiwa.dragontravel.core.DragonTravelMain;
import eu.phiwa.dragontravel.core.objects.Flight;
import eu.phiwa.dragontravel.core.permissions.PermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class FlightsDB {

    private File dbFlightsFile;
    private FileConfiguration dbFlightsConfig;
    private ConfigurationSection flightSection;

    public FlightsDB() {
        init();
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void create() {
        if (dbFlightsFile.exists()) {
            return;
        }
        try {
            dbFlightsFile.createNewFile();
            copy(DragonTravelMain.getInstance().getResource("databases/flights.yml"), dbFlightsFile);
            Bukkit.getLogger().log(Level.INFO, "Created flights-database.");
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create the flights-database!");
        }


    }

    /**
     * Creates a new flight.
     *
     * @param flight Flight to create.
     * @return Returns true if the flight was created successfully, false if not.
     */
    public boolean saveFlight(Flight flight) {
        flightSection.set(flight.getName(), flight);

        try {
            dbFlightsConfig.save(dbFlightsFile);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not write new flight to config.");
            return false;
        }
    }

    /**
     * Deletes the given flight.
     *
     * @param flightName Name of the flight to delete
     * @return True if successful, false if not.
     */
    public boolean deleteFlight(String flightName) {
        flightSection.set(flightName.toLowerCase(), null);

        try {
            dbFlightsConfig.save(dbFlightsFile);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().info("[DragonTravel] [Error] Could not delete flight from config.");
            return false;
        }
    }


    /**
     * Returns the details of the flight with the given name.
     *
     * @param flightName Name of the flight which should be returned.
     * @return The flight as a flight-object.
     */
    public Flight getFlight(String flightName) {
        flightName = flightName.toLowerCase();
        Object obj = flightSection.get(flightName, null);
        if (obj == null) {
            return null;
        }
        if (obj instanceof ConfigurationSection) {
            Flight f = new Flight(flightName, ((ConfigurationSection) obj).getValues(true));
            f.setName(flightName);
            saveFlight(f);
            return f;
        } else {
            Flight f = (Flight) obj;
            f.setName(flightName);
            return f;
        }
    }

    public void init() {
        dbFlightsFile = new File("plugins/DragonTravel/databases", "flights.yml");
        try {
            create();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not initialize the flights-database.");
            e.printStackTrace();
        }
        dbFlightsConfig = new YamlConfiguration();
        load();

        flightSection = dbFlightsConfig.getConfigurationSection("Flights");
        if (flightSection == null) {
            flightSection = dbFlightsConfig.createSection("Flights");
        }
    }

    private void load() {
        try {
            dbFlightsConfig.load(dbFlightsFile);
            Bukkit.getLogger().log(Level.INFO, "Loaded flights-database.");
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "No flights-database found");
            e.printStackTrace();
        }
    }

    public void showFlights(CommandSender sender) {
        sender.sendMessage("Available flights: ");
        int i = 0;
        for (String string : dbFlightsConfig.getConfigurationSection("Flights").getKeys(false)) {
            Flight flight = getFlight(string);
            if (flight != null) {
                sender.sendMessage(" - " + (sender instanceof Player ? (PermissionsHandler.hasFlightPermission((Player) sender, flight.getName()) ? ChatColor.GREEN : ChatColor.RED) : ChatColor.AQUA) + flight.getDisplayName());
                i++;
            }
        }
        sender.sendMessage(String.format("(total %d)", i));
    }
}