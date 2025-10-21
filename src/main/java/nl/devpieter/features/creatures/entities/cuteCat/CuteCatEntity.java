package nl.devpieter.features.creatures.entities.cuteCat;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;

public class CuteCatEntity extends EntityCreature {

    public CuteCatEntity() {
        super(EntityType.CAT);

        CatMeta catMeta = (CatMeta) getEntityMeta();
        catMeta.setTamed(true);
    }
}
