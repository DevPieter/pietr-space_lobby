package nl.devpieter;

import nl.devpieter.events.WorldManagerInitializedEvent;
import nl.devpieter.features.creatures.CreatureManager;
import nl.devpieter.features.farming.FarmManager;
import nl.devpieter.features.parkour.ParkourManager;
import nl.devpieter.features.portals.PortalManager;
import nl.devpieter.features.signs.SignManager;
import nl.devpieter.sees.annotations.SEventListener;
import nl.devpieter.sees.listener.SListener;

public class MainListener implements SListener {

    @SEventListener
    public void onWorldManagerInitialized(WorldManagerInitializedEvent event) {
        SignManager signManager = SignManager.getInstance();
        signManager.init();

        CreatureManager creatureManager = CreatureManager.getInstance();
        creatureManager.init();

        ParkourManager parkourManager = ParkourManager.getInstance();
        parkourManager.init();

        PortalManager portalManager = PortalManager.getInstance();
        portalManager.init();

        FarmManager farmManager = FarmManager.getInstance();
        farmManager.init();
    }
}
