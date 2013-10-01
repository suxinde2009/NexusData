package org.nexusdata.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.utils.StringUtil;


public class PersistentStoreCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStoreCoordinator.class);

    private final Map<UUID, PersistentStore> m_storeUuidToPersisentStore = new LinkedHashMap<UUID, PersistentStore>();
    private final ObjectModel m_model;

    public PersistentStoreCoordinator(ObjectModel model) {
        m_model = model;
    }

    public void addStore(PersistentStore store) {
        if (store.getCoordinator() != null && store.getCoordinator() != this) {
            throw new IllegalStateException("PersistentStore " + store + " already assigned to another coordinator");
        }
        store.setPersistentStoreCoordinator(this);
        store.loadMetadata();

        if (store.getUuid() == null) {
            throw new RuntimeException("Did not get permanent UUID from store: " + store);
        }

        m_storeUuidToPersisentStore.put(store.getUuid(), store);

        LOG.info("Added persistent store " + store);
    }

    public void removeStore(PersistentStore store) {
        m_storeUuidToPersisentStore.remove(store.getUuid());
        store.setPersistentStoreCoordinator(null);
    }

    public PersistentStore getPersistentStore(UUID uuid) {
        return m_storeUuidToPersisentStore.get(uuid);
    }

    public List<PersistentStore> getPersistentStores() {
        return new ArrayList<PersistentStore>(m_storeUuidToPersisentStore.values());
    }

    public ObjectModel getModel() {
        return m_model;
    }

    public ObjectID objectIDFromUri(URI objectIDUri) {
        if (!objectIDUri.getScheme().equals("nexusdata")) {
            throw new IllegalArgumentException("");
        } else if (StringUtil.isBlank(objectIDUri.getAuthority())) {
            throw new IllegalArgumentException("Cannot create ObjectID from temporary ID");
        }

        String[] parts = objectIDUri.getPath().split("/");
        if (parts.length < 3 || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw new IllegalArgumentException("Invalid ObjectID URI format");
        }

        UUID storeUuid = UUID.fromString(objectIDUri.getAuthority());
        EntityDescription<?> entity = m_model.getEntity(parts[1]);
        Object referenceObject = parts[2];
        try {
            referenceObject = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            // ignore; treat ref object as string
        }

        return new ObjectID(m_storeUuidToPersisentStore.get(storeUuid), entity, referenceObject);
    }

    void executeFetchRequest() {

    }

    void save(SaveChangesRequest saveRequest) {

    }
}