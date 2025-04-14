package de.terranova.nations.database.dao;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.poly.PropertyRegion;
import de.terranova.nations.regions.poly.PropertyState;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for storing minimal property data in `poly_regions` table.
 * We do NOT store polygon corners, because we rely on WorldGuard's own data.
 */
public class PropertyRegionDAO {
    private static final String INSERT_OR_UPDATE = """
       INSERT INTO poly_regions (RUUID, name, type, price, state, parent, world)
       VALUES (?, ?, ?, ?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE
         name=VALUES(name),
         price=VALUES(price),
         state=VALUES(state),
         parent=VALUES(parent),
         world=VALUES(world);
    """;

    private static final String SELECT_PROP = """
       SELECT ruuid, name, type, price, state, parent, world
         FROM poly_regions
        WHERE name=?;
    """;

    private static final String DELETE_PROP = """
       DELETE FROM poly_regions WHERE RUUID=?;
    """;

    /**
     * Save or update a property region row.
     * 'worldName' is stored just for reference if you want it.
     * If you don’t want that, you can remove from table.
     */
    public static void saveProperty(PropertyRegion prop, String worldName) {
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE)) {
            ps.setString(1, prop.getId().toString());
            ps.setString(2, prop.getName());
            ps.setString(3, prop.getState().name());
            ps.setInt(4, prop.getPrice());
            ps.setString(5, prop.getState().name());
            ps.setString(6, prop.getParent() == null ? null : prop.getParent().toString());
            ps.setString(7, worldName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load property region by UUID from DB.
     */
    public static Optional<PropertyRegion> loadProperty(String propertyName) {
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PROP)) {
            ps.setString(1, propertyName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String uuid  = rs.getString("ruuid");
                int price    = rs.getInt("price");
                PropertyState state = PropertyState.valueOf(rs.getString("state"));
                String parentStr = rs.getString("parent");
                UUID parentUuid = (parentStr == null) ? null : UUID.fromString(parentStr);
                PropertyRegion prop = new PropertyRegion(propertyName, UUID.fromString(uuid));
                prop.setPrice(price);
                prop.setParent(parentUuid);
                prop.setState(state);
                return Optional.of(prop);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Delete property from DB
     */
    public static void deleteProperty(UUID ruuid) {
        try (Connection conn = NationsPlugin.hikari.dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_PROP)) {
            ps.setString(1, ruuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
