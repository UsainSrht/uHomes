package me.usainsrht.uhomes.manager;

import me.usainsrht.uhomes.UHomes;
import me.usainsrht.uhomes.claim_interfaces.ClaimAPI;

public class ClaimManager {

    private UHomes plugin;
    private ClaimAPI claimAPI;

    public ClaimManager(UHomes plugin, ClaimAPI claimAPI) {
        this.plugin = plugin;
        this.claimAPI = claimAPI;
    }

    public void setClaimAPI(ClaimAPI claimAPI) {
        this.claimAPI = claimAPI;
    }

    public UHomes getPlugin() {
        return plugin;
    }

    public ClaimAPI getClaimAPI() {
        return claimAPI;
    }

}
