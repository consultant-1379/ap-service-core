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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.itpf.datalayer.dps.query.ContainmentRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.datalayer.dps.query.Query;
import com.ericsson.oss.itpf.datalayer.dps.query.QueryBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.Restriction;
import com.ericsson.oss.itpf.datalayer.dps.query.RestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.SortDirection;
import com.ericsson.oss.itpf.datalayer.dps.query.TypeRestrictionBuilder;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.Projection;
import com.ericsson.oss.itpf.datalayer.dps.query.projection.ProjectionBuilder;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;

/***
 * Common set of executable DPS queries. Supports invocation in both CDI and NON-CDI contexts.
 */
public class DpsQueries {

    private final TransactionalExecutor executor = new TransactionalExecutor();

    private DataPersistenceService dps;

    /**
     * Execute the DPS query for PO/MO.
     */
    public class DpsQueryExecutor<T extends PersistenceObject> {

        private final Query<? extends RestrictionBuilder> query;

        public DpsQueryExecutor(final Query<? extends RestrictionBuilder> query) {
            this.query = query;
        }

        public Iterator<T> execute() {
            return getDps().getLiveBucket().getQueryExecutor().execute(query);
        }

        public Long executeCount() {
            return getDps().getLiveBucket().getQueryExecutor().executeCount(query);
        }
    }

    /**
     * Execute the DPS projection query.
     */
    public class DpsProjectionQueryExecutor {

        private final Query<? extends RestrictionBuilder> query;
        private final Projection firstProjection;
        private final Projection[] remainingProjections;

        public DpsProjectionQueryExecutor(final Query<? extends RestrictionBuilder> query, final Projection firstProjection,
            final Projection... remainingProjections) {
            this.query = query;
            this.firstProjection = firstProjection;
            this.remainingProjections = remainingProjections;
        }

        public List<Object[]> execute() {
            return getDps().getLiveBucket().getQueryExecutor().executeProjection(query, firstProjection, remainingProjections);
        }
    }

    /**
     * Type query to find MOs of the specified type and name.
     *
     * @param moName
     *            the name of the MO
     * @param moType
     *            the type of the MO
     * @param namespace
     *            the namespace of the MO to find
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findMoByName(final String moName, final String moType, final String namespace) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, moType);
        final Restriction restriction = typeQuery.getRestrictionBuilder().equalTo(ObjectField.NAME, moName);
        typeQuery.setRestriction(restriction);
        return new DpsQueryExecutor<>(typeQuery);
    }

    /**
     * Type query to find MOs of the specified type and name.
     * <p>
     * Executes in its own transaction, using {@link TransactionalExecutor}.
     *
     * @param moName
     *            the name of the MO
     * @param moType
     *            the type of the MO
     * @param namespace
     *            the namespace of the MO to find
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findMoByNameInTransaction(final String moName, final String moType, final String namespace) {
        final Callable<DpsQueryExecutor<ManagedObject>> updateMoCallable = () -> findMoByName(moName, moType, namespace);
        try {
            return executor.execute(updateMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error finding MO with name %s in %s:%s", moName, moType, namespace), e);
        }
    }

    /**
     * Type query to find all child MOs of the specified types i.e. one or more types can be specified in the search.
     * <p>
     * Sorts MOs alphabetically by {@link ObjectField#NAME}.
     *
     * @param parentFdn
     *            the FDN of the parent MO
     * @param childTypes
     *            the types of the child MO (can be one or more)
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findChildMosOfTypes(final String parentFdn, final String... childTypes) {
        return findChildMosOfTypes(parentFdn, null, childTypes);
    }

    /**
     * Type query to find all child MOs of the specified types i.e. one or more types can be specified in the search.
     *
     * @param parentFdn
     *            the FDN of the parent MO
     * @param sortOrder
     *            the order in which to sort the returned MOs
     * @param childTypes
     *            the types of the child MO (can be one or more)
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findChildMosOfTypes(final String parentFdn, final ObjectField sortOrder,
        final String... childTypes) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<ContainmentRestrictionBuilder> containmentQuery = queryBuilder.createContainmentQuery(parentFdn);
        final Restriction restriction = containmentQuery.getRestrictionBuilder().in(ObjectField.TYPE, (Object[]) childTypes);
        containmentQuery.setRestriction(restriction);
        if (sortOrder != null) {
            containmentQuery.addSortingOrder(sortOrder, SortDirection.ASCENDING);
        }
        return new DpsQueryExecutor<>(containmentQuery);
    }

    /**
     * Type query to find all child MOs of the specified types i.e. one or more types can be specified in the search.
     * <p>
     * Executes in its own transaction, using {@link TransactionalExecutor}.
     *
     * @param parentFdn
     *            the FDN of the parent MO
     * @param childTypes
     *            the types of the child MO (can be one or more)
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findChildMosOfTypesInOwnTransaction(final String parentFdn,
        final String... childTypes) {
        final Callable<DpsQueryExecutor<ManagedObject>> updateMoCallable = () -> findChildMosOfTypes(parentFdn, null, childTypes);
        try {
            return executor.execute(updateMoCallable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error finding child MO of %s of type %s", parentFdn, Arrays.asList(childTypes)), e);
        }
    }

    /**
     * Containment query to find all child MOs of the specified parent.
     *
     * @param parentFdn
     *            the FDN of the parent MO
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findAllChildMos(final String parentFdn) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<ContainmentRestrictionBuilder> containmentQuery = queryBuilder.createContainmentQuery(parentFdn);
        return new DpsQueryExecutor<>(containmentQuery);

    }

    /**
     * Type query to find all MOs of a specified type in the namespace.
     *
     * @param moType
     *            the MO type
     * @param namespace
     *            the namespace
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findMosByType(final String moType, final String namespace) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, moType);
        return new DpsQueryExecutor<>(typeQuery);
    }

    /**
     * Type query to find all POs of a specified type in the namespace.
     *
     * @param poType
     *            the PO type
     * @param namespace
     *            the namespace
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<PersistenceObject> findPosByType(final String poType, final String namespace) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, poType);
        return new DpsQueryExecutor<>(typeQuery);
    }

    /**
     * Type query to find all MOs with the specified attribute value.
     *
     * @param attributeName
     *            the name of the mo attribute
     * @param expectedAttributeValue
     *            the name of the expected attribute value
     * @param namespace
     *            the namespace of the MOs
     * @param moType
     *            the MO type
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findMosWithAttributeValue(final String attributeName, final Object expectedAttributeValue,
        final String namespace,
        final String moType) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, moType);
        typeQuery.setRestriction(typeQuery.getRestrictionBuilder().equalTo(attributeName, expectedAttributeValue));
        return new DpsQueryExecutor<>(typeQuery);
    }

    /**
     * Projection query to return only specified attributes for all MOs of a specified type in the namespace.
     *
     * @param namespace
     *            the namespace of the MO
     * @param moType
     *            the MO type
     * @param firstAttribute
     *            the first attribute
     * @param remainingAttributes
     *            the remaining attributes
     * @return <code>DpsProjectionQueryExecutor</code>
     */
    public DpsProjectionQueryExecutor getMoAttributeValues(final String namespace, final String moType, final String firstAttribute,
        final String... remainingAttributes) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, moType);

        final Projection initialProjection = createProjections(firstAttribute)[0];
        final Projection[] furtherProjections = createProjections(remainingAttributes);

        return new DpsProjectionQueryExecutor(typeQuery, initialProjection, furtherProjections);
    }

    /**
     * Query to return MOs of a given type in the namespace, which have the specified attribute.
     *
     * @param attributeName
     *            the name of the attribute
     * @param namespace
     *            the namespace of the MO
     * @param moType
     *            the MO type
     * @return <code>DpsQueryExecutor</code>
     */
    public DpsQueryExecutor<ManagedObject> findMosWithAttribute(final String attributeName, final String namespace, final String moType) {
        final QueryBuilder queryBuilder = getDps().getQueryBuilder();
        final Query<TypeRestrictionBuilder> typeQuery = queryBuilder.createTypeQuery(namespace, moType);
        typeQuery.setRestriction(typeQuery.getRestrictionBuilder().hasAttributeNamed(attributeName));
        return new DpsQueryExecutor<>(typeQuery);
    }

    private static Projection[] createProjections(final String... attributes) {
        final Map<String, ObjectField> nonModeledFields = getNonModeledFields();
        final Projection[] projections = new Projection[attributes.length];
        for (int i = 0; i < projections.length; i++) {
            if (nonModeledFields.containsKey(attributes[i])) {
                projections[i] = ProjectionBuilder.field(nonModeledFields.get(attributes[i]));
            } else {
                projections[i] = ProjectionBuilder.attribute(attributes[i]);
            }
        }
        return projections;
    }

    @SuppressWarnings("deprecation")
    private static Map<String, ObjectField> getNonModeledFields() {
        final Map<String, ObjectField> nameToObjectField = new HashMap<>();

        for (final ObjectField objectField : ObjectField.values()) {
            nameToObjectField.put(objectField.getFieldName(), objectField); //NOSONAR
        }
        return nameToObjectField;
    }

    private DataPersistenceService getDps() {
        if (dps == null) {
            dps = new ServiceFinderBean().find(DataPersistenceService.class);
        }
        return dps;
    }
}
