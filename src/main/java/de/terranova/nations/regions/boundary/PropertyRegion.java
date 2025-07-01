package de.terranova.nations.regions.boundary;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.terranova.nations.database.dao.GridRegionDAO;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.regions.modules.realEstate.RealEstateAgent;
import de.terranova.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class PropertyRegion extends BoundaryRegion implements HasParent<SettleRegion>, CanBeSold {
    public static final String REGION_TYPE = "property";

    private SettleRegion parent;
    private RealEstateAgent realEstateAgent;

    public PropertyRegion(String name, UUID ruuid, SettleRegion parent) {
        super(name, ruuid, REGION_TYPE);
        this.addNameToCache(name);
        setParent(parent);
        this.region = getWorldguardRegion();
        if(region != null) {
            this.realEstateAgent = new RealEstateAgent(this);
        }

    }

    //GridRegionType
    @Override
    public void onBoundaryCreation(Player p) {
        System.out.println("onBoundaryCreation");
        GridRegionDAO.insertParent(this.id,parent.getId());
        this.realEstateAgent = new RealEstateAgent(this);
        p.sendMessage(Chat.greenFade("Deine Stadt " + name + " wurde erfolgreich gegründet."));
    }

    @Override
    public SettleRegion getParent() {
        return parent;
    }

    @Override
    public void setParentRaw(SettleRegion parent) {
        this.parent = parent;
    }

    @Override
    public RealEstateAgent getAgent() {
        return realEstateAgent;
    }
}