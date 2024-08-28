/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.cm;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.MibRootBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Common set of {@link DataPersistenceService} operations that are executed in a new transaction. Supports invocation in both CDI and NON-CDI
 * contexts.
 */
@Stateless
public class DpsOperations {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EServiceRef
    private DataPersistenceService dps;
    private TransactionalExecutor executor = new TransactionalExecutor(); //NOPMD

    /**
     * Update a {@link ManagedObject} in a new transaction. The updated {@link ManagedObject} will be committed on return from this method.
     *
     * @param moFdn
     *            the FDN of the {@link ManagedObject} to be updated
     * @param attrName
     *            the name of the attribute to be updated
     * @param attrValue
     *            the value of the attribute to be updated
     */
    public void updateMo(final String moFdn, final String attrName, final Object attrValue) {
        final Callable<Void> updateMoCallable = () -> {
            final ManagedObject mo = getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
            if (mo != null) {
                updateAttribute(moFdn, attrName, attrValue, mo);
            } else {
                logger.info("Nothing to update -> {} does not exist", moFdn);
            }
            return null;
        };

        try {
            executor.execute(updateMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error updating %s, attrName -> %s, value -> %s", moFdn, attrName, attrValue), e);
        }
    }

    /**
     * Update a {@link ManagedObject} in a new transaction. The updated {@link ManagedObject} will be committed on return from this method.
     *
     * @param moFdn
     *            the FDN of the {@link ManagedObject} to be updated
     * @param attrName
     *            the name of the attribute to be updated
     * @param attrValue
     *            the value of the attribute to be updated
     * @throws ApServiceException
     *             thrown if there is an error updating the {@link ManagedObject}, or if it does not exist
     */
    public void updateMoWithFailure(final String moFdn, final String attrName, final Object attrValue) {
        final Callable<Void> updateMoCallable = () -> {

            final ManagedObject mo = getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
            if (mo == null) {
                throw new ApServiceException(String.format("MO [%s] does not exist", moFdn));
            }

            mo.setAttribute(attrName, attrValue);
            logger.debug("Updated {}, name={}, value={}", moFdn, attrName, attrValue);
            return null;
        };

        try {
            executor.execute(updateMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error updating %s, attrName -> %s, value -> %s", moFdn, attrName, attrValue), e);
        }
    }

    /**
     * Update a {@link ManagedObject} in a new transaction. The updated {@link ManagedObject} will be committed on return from this method.
     *
     * @param moFdn
     *            the FDN of the {@link ManagedObject} to be updated
     * @param attributesMap
     *            the names and the values of the attributes to be updated
     */
    public void updateMo(final String moFdn, final Map<String, Object> attributesMap) {
        final Callable<Void> updateMoCallable = () -> {
            final ManagedObject mo = getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
            if (mo != null) {
                mo.setAttributes(attributesMap);
                logger.debug("Updated {}, with values {}", moFdn, attributesMap);
            } else {
                logger.info("Nothing to update -> {} does not exist", moFdn);
            }
            return null;
        };

        try {
            executor.execute(updateMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(
                String.format("Error updating %s, with multiple attributes %s", moFdn, attributesMap), e);
        }
    }

    /**
     * Read all attributes from a MO in a new transaction.
     *
     * @param moFdn
     *            the FDN of the MO whose attributes are to be read
     *
     * @return a map of attributes
     */
    public Map<String, Object> readMoAttributes(final String moFdn) {
        final Callable<Map<String, Object>> readMoCallable = () -> {
            final ManagedObject mo = getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
            return mo.getAllAttributes();
        };

        try {
            return executor.execute(readMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error reading attributes from MO %s", moFdn), e);
        }
    }

    /**
     * Deletes a managed object in a new transaction. Does nothing if the MO does not exist.
     *
     * @param moFdn
     *            the FDN of the MO to be deleted
     */
    public void deleteMo(final String moFdn) {
        final Callable<Void> deleteMoCallable = () -> {

            final DataBucket liveBucket = getDataPersistenceService().getLiveBucket();
            final ManagedObject mo = liveBucket.findMoByFdn(moFdn);
            if (mo != null) {
                liveBucket.deletePo(mo);
                logger.debug("Deleted MO {}", moFdn);
            } else {
                logger.info("Nothing to delete -> {} does not exist", moFdn);
            }
            return null;
        };

        try {
            executor.execute(deleteMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error deleting %s", moFdn), e);
        }
    }

    /**
     * Creates a root MO in a new transaction.
     *
     * @param fdnToCreate
     *            the FDN of MO being created
     * @param modelData
     *            the namespace and version
     * @param attributes
     *            the managed object attributes
     * @return <code>ManagedObject</code>
     */
    public ManagedObject createRootMo(final String fdnToCreate, final ModelData modelData, final Map<String, Object> attributes) {
        final Callable<ManagedObject> createMoCallable = getCreateMoCallable(fdnToCreate, modelData, attributes);

        try {
            return executor.execute(createMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error creating %s: %s, attributes-> %s", fdnToCreate, e.getMessage(), attributes), e);
        }
    }

    public boolean existsMoByFdn(final String moFdn) {
        return getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn) != null;
    }

    private void updateAttribute(final String moFdn, final String attrName, final Object attrValue, final ManagedObject mo) {
        try {
            mo.setAttribute(attrName, attrValue);
            logger.debug("Updated {}, name={}, value={}", moFdn, attrName, attrValue);
        } catch (final Exception e) {
            logger.info("Error updating {} for {}", attrName, moFdn);
        }
    }

    private Callable<ManagedObject> getCreateMoCallable(final String fdnToCreate, final ModelData modelData, final Map<String, Object> attributes) {
        return () -> {
            final MibRootBuilder mibRootBuilder = createMibRootBuilder(fdnToCreate, modelData, attributes);
            final ManagedObject mo = mibRootBuilder.create();
            logger.debug("Created MO {}, attributes -> {}", fdnToCreate, attributes);
            return mo;
        };
    }

    private MibRootBuilder createMibRootBuilder(final String fdn, final ModelData modelData, final Map<String, Object> attributes) {
        final FDN moFdn = FDN.get(fdn);
        final String moName = moFdn.getRdnValue();
        final String moType = moFdn.getType();
        final String moParent = moFdn.getParent();
        final String modelName = modelData.getNameSpace();
        final String version = modelData.getVersion();

        final DataBucket liveBucket = getDataPersistenceService().getLiveBucket();

        final MibRootBuilder mibRootBuilder = liveBucket.getMibRootBuilder()
            .name(moName)
            .type(moType)
            .namespace(modelName)
            .version(version)
            .addAttributes(attributes);

        if (moParent != null) {
            final ManagedObject parentMo = liveBucket.findMoByFdn(moParent);
            mibRootBuilder.parent(parentMo);
        }
        return mibRootBuilder;
    }

    /**
     * Executes the specified MO action in a new transaction.
     *
     * @param moFdn
     *            the FDN of the MO
     * @param actionName
     *            the name of the action
     */
    public void performMoAction(final String moFdn, final String actionName) {
        final Callable<Void> deleteMoCallable = () -> {
            final ManagedObject mo = getDataPersistenceService().getLiveBucket().findMoByFdn(moFdn);
            if (mo != null) {
                mo.performAction(actionName, Collections.<String, Object> emptyMap());
                logger.debug("Executed action {} for MO {}", actionName, moFdn);
            }
            return null;
        };

        try {
            executor.execute(deleteMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error perfoming action %s for %s", actionName, moFdn), e);
        }
    }

    public DataPersistenceService getDataPersistenceService() {
        if (dps == null) {
            dps = new ServiceFinderBean().find(DataPersistenceService.class);
        }
        return dps;
    }
}
