package com.rick.db.repository;

import com.rick.common.util.ClassUtils;
import com.rick.common.util.IdGenerator;
import com.rick.common.util.Maps;
import com.rick.db.repository.support.TableMeta;
import com.rick.db.repository.support.TableMetaResolver;
import com.rick.db.util.OperatorUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rick.db.repository.support.Constants.COLUMN_NAME_SEPARATOR_REGEX;

/**
 * @author Rick.Xu
 * @date 2025/8/14 11:52
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated // 必须加入
public class EntityDAOImpl<T, ID> implements EntityDAO<T, ID> {

    @Resource
    @Getter
    private TableDAO tableDAO;

    @Getter
    private TableMeta<T> tableMeta;

    private static final ThreadLocal<Collection<Object>> threadLocalEntity = ThreadLocal.withInitial(ArrayList::new);

    public EntityDAOImpl() {
        Class<?>[] actualTypeArguments = ClassUtils.getClassGenericsTypes(getClass());
        this.tableMeta = TableMetaResolver.resolve(actualTypeArguments[0]);
        init();
    }

    public EntityDAOImpl(NamedParameterJdbcTemplate jdbcTemplate, Class<T> entityClass) {
        this(new TableDAOImpl(jdbcTemplate), entityClass);
    }

    public EntityDAOImpl(TableDAO tableDAO, Class<T> entityClass) {
        this.tableDAO = tableDAO;
        this.tableMeta = TableMetaResolver.resolve(entityClass);
        init();
    }

    private void init() {
        EntityDAOManager.register(tableMeta.getEntityClass(), this);
    }

    @Override
    public Optional<T> selectById(ID id) {
//        return namedParameterJdbcTemplate.getJdbcTemplate().queryForObject("SELECT "+selectColumn+" FROM "+ tableName +" WHERE id = ?", new BeanPropertyRowMapper<>(clazz), id);
        List<T> list = select("id = ?", id);
        return OperatorUtils.expectedAsOptional(list);
    }

    @Override
    public List<T> selectByIds(Collection<ID> ids) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", ids);
        return select("id IN (:ids)", paramMap);
    }

    @Override
    public List<T> selectAll() {
        return select(null);
    }

    @Override
    public List<T> select(String condition, Object... args) {
        return selectWithColumns(tableMeta.getSelectColumn(), condition, args);
    }

    @Override
    public List<T> select(String condition, Map<String, ?> paramMap) {
        return select(tableMeta.getSelectColumn(), condition, paramMap);
    }

    @Override
    public List<T> selectWithColumns(String columns, String condition, Object... args) {
        return select(tableMeta.getEntityClass(), columns, condition, args);
    }

    @Override
    public List<T> select(String columns, String condition, Map<String, ?> paramMap) {
        return select(tableMeta.getEntityClass(), columns, condition, paramMap);
    }

    @Override
    public Boolean exists(String condition, Object... args) {
        return select(String.class, "1", (StringUtils.isBlank(condition) ? "" : condition)+ " LIMIT 1", args).size() > 0 ? true : false;
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, Object... args) {
        List<E> list = tableDAO.select(clazz, "SELECT " + columns + " FROM " + tableMeta.getTableName() + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), args);
        cascadeSelect(clazz, (List<T>) list);
        return list;
    }

    @Override
    public List<T> select(String condition, T example) {
        return select(tableMeta.getSelectColumn(), condition, example);
    }

    @Override
    public List<T> select(String columns, String condition, T example) {
        return select(tableMeta.getEntityClass(), columns, condition, example);
    }

    @Override
    public Boolean exists(String condition, T example) {
        return select(String.class, "1", (StringUtils.isBlank(condition) ? "" : condition)+ " LIMIT 1", example).size() > 0 ? true : false;
    }

    @Override
    public <E> Optional<E> selectOne(Class<E> clazz, String columns, String condition, T example) {
        return OperatorUtils.expectedAsOptional(select(clazz, columns, condition, example));
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, T example) {
        return select(clazz, columns, condition, getArgsFromEntity(example, true));
    }

    @Override
    public <E> List<E> select(Class<E> clazz, String columns, String condition, Map<String, ?> paramMap) {
        List<E> list = tableDAO.select(clazz, "SELECT " + columns + " FROM " + tableMeta.getTableName() + (StringUtils.isBlank(condition) ? "" : " WHERE " + condition), paramMap);
        cascadeSelect(clazz, (List<T>) list);
        return list;
    }

    public void cascadeSelect(Class<?> clazz, List<T> list) {
        if (clazz == tableMeta.getEntityClass() && hasSelectReference()) {
            // 级联查询
            selectReference(list);
        }
    }

    private boolean hasSelectReference() {
        return MapUtils.isNotEmpty(tableMeta.getReferenceMap()) && tableMeta.getReferenceMap().values().stream().anyMatch(reference -> (Objects.nonNull(reference.getManyToMany()) && reference.getManyToMany().cascadeSelect()) || (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeSelect()) || (Objects.nonNull(reference.getManyToOne()) && reference.getManyToOne().cascadeSelect()) || Objects.nonNull(reference.getSelect()));
    }

    private void selectReference(List<T> list) {
        // 级联查询(@OneToMany @ManyToMany @ManyToOne)
        if (CollectionUtils.isNotEmpty(list)) {
            threadLocalEntity.get().addAll(list);
            Set<ID> ids = list.stream().map(t -> (ID)getIdValue(t)).collect(Collectors.toSet());

            List<?> referenceList;
            for (Map.Entry<Field, TableMeta.Reference> fieldReferenceEntry : tableMeta.getReferenceMap().entrySet()) {
                TableMeta.Reference reference = fieldReferenceEntry.getValue();

                EntityDAO referenceDAO = EntityDAOManager.getDAO(reference.getReferenceClass());
                if (Objects.nonNull(reference.getManyToMany()) && reference.getManyToMany().cascadeSelect()) {
                    List<Object[]> refTable = tableDAO.select("SELECT " + reference.getManyToMany().joinColumnId() + ", " + reference.getManyToMany().inverseJoinColumnId() + " FROM " + reference.getManyToMany().tableName() + " WHERE " + reference.getManyToMany().joinColumnId() + " IN (:ids)", Maps.of("ids", ids), (JdbcTemplateCallback<Object[]>) (jdbcTemplate, sql, paramMap) -> jdbcTemplate.query(sql, paramMap, (rs, rowNum) -> new Object[]{rs.getObject(1, tableMeta.getIdMeta().getIdClass()), rs.getObject(2, referenceDAO.getTableMeta().getIdMeta().getIdClass())})
                    );

                    final List<Object> mergedReferenceList = new ArrayList<>();
                    Set<Object> queryRefIdsSet = new HashSet<>();

                    for (Object[] ref : refTable) {
                        Object refId = ref[1];
                        Optional<?> optional = threadLocalEntity.get().stream().filter(entity -> entity.getClass() == reference.getReferenceClass() && Objects.equals(getIdValue(entity), refId)).findFirst();
                        if (optional.isPresent()) {
                            mergedReferenceList.add(optional.get());
                        } else {
                            queryRefIdsSet.add(refId);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(queryRefIdsSet)) {
                        referenceList = referenceDAO.select(tableMeta.getIdMeta().getIdPropertyName() + " IN (:ids)", Maps.of("ids", queryRefIdsSet));
                        mergedReferenceList.addAll(referenceList);
                    }

                    Map<Object, List<Object>> map = new HashMap<>();

                    for (int i = 0; i < refTable.size(); i++) {
                        Object[] ref = refTable.get(i);

                        List<Object> dataList = map.get(ref[0]);
                        if (CollectionUtils.isEmpty(dataList)) {
                            dataList = new ArrayList<>();
                            map.put(ref[0], dataList);
                        }

                        dataList.add(mergedReferenceList.stream().filter(e -> Objects.equals(ref[1], getIdValue(e))).findFirst().get());
                    }

                    for (T t : list) {
                        setPropertyValue(t, reference.getField().getName(), ObjectUtils.defaultIfNull(map.get(getIdValue(t)), Collections.emptyList()));
                    }
                } else if (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeSelect()) {
                    String mappedBy = Objects.nonNull(reference.getOneToMany()) ? reference.getOneToMany().mappedBy() : reference.getOneToMany().mappedBy();

                    referenceList = referenceDAO.select(StringUtils.defaultIfBlank(reference.getOneToMany().joinColumnId(), tableMeta.getReferenceColumnId()) + " IN (:ids)", Maps.of("ids", ids));

                    final List<Object> mergedReferenceList = new ArrayList<>();
                    for (Object queryEntity : referenceList) {
                        Optional<Object> optional = threadLocalEntity.get().stream().filter(entity -> entity.getClass() == queryEntity.getClass() && Objects.equals(getIdValue(entity), getIdValue(queryEntity))).findFirst();
                        if (optional.isPresent()) {
                            mergedReferenceList.add(optional.get());
                        } else {
                            mergedReferenceList.add(queryEntity);
                        }
                    }
                    referenceList = mergedReferenceList;

                    Map<Object, List<Object>> map = referenceList.stream().collect(Collectors.groupingBy(e -> parsingColumnValue(reference.getField(), tableMeta.getFieldColumnNameMap().get(reference.getField()), getPropertyValue(e, mappedBy)), Collectors.toList()));
                    for (T t : list) {
                        if (reference.getOneToMany().oneToOne()) {
                            List<Object> groupReferenceList = map.get(getIdValue(t));
                            setPropertyValue(t, reference.getField().getName(), CollectionUtils.isEmpty(groupReferenceList) ? null : groupReferenceList.iterator().next());
                        } else {
                            setPropertyValue(t, reference.getField().getName(), map.get(getIdValue(t)));
                        }
                    }
                } else if (Objects.nonNull(reference.getManyToOne()) && reference.getManyToOne().cascadeSelect()) {
                    Set<?> refIds = list.stream().map(e -> getPropertyValue(e, reference.getField().getName()))
                            .filter(e -> Objects.nonNull(e))
                            .map(e -> getIdValue(e))
                            .collect(Collectors.toSet());

                    for (T t : list) {
                        threadLocalEntity.get().stream().filter(entity -> entity.getClass() == reference.getReferenceClass() && Objects.equals(getIdValue(entity), parsingColumnValue(reference.getField(), tableMeta.getFieldColumnNameMap().get(reference.getField()), getPropertyValue(t, reference.getField().getName()))))
                                .findFirst().ifPresent(entity -> {
                                    setPropertyValue(t, reference.getField().getName(), entity);
                                    refIds.remove(getIdValue(entity));
                                });
                    }

                    if (CollectionUtils.isNotEmpty(refIds)) {
                        referenceList = referenceDAO.select(tableMeta.getIdMeta().getIdPropertyName() + " IN (:ids)", Maps.of("ids", refIds));
                        Map<?, Object> map = referenceList.stream().collect(Collectors.toMap(e -> getIdValue(e), Function.identity()));
                        for (T t : list) {
                            Object refEntity = getPropertyValue(t, reference.getField().getName());
                            setPropertyValue(t, reference.getField().getName(), map.get(getIdValue(refEntity)));
                        }
                    }
                } else if (Objects.nonNull(reference.getSelect())) {
                    Select select = reference.getSelect();
                    String[] mappingKv = StringUtils.isBlank(select.params()) ? null : select.params().split(COLUMN_NAME_SEPARATOR_REGEX);
                    Set<String> notNullKeys = Arrays.stream(select.nullWhenParamsIsNull()).collect(Collectors.toSet());
                    for (T t : list) {
                        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(t);
                        boolean valueNull = false;
                        Map<String, Object> params = null;
                        // 获取参数
                        if (ArrayUtils.isNotEmpty(mappingKv)) {
                            params = new HashMap<>(mappingKv.length);
                            for (String kv : mappingKv) {
                                String[] kvArr = kv.split("@");
                                Object value = beanWrapper.getPropertyValue(kvArr[1]);
                                params.put(kvArr[0], value);
                                if (notNullKeys.contains(kvArr[0]) && Objects.isNull(value)) {
                                    valueNull = true;
                                    break;
                                }

                                if (Objects.isNull(value)) {
                                    params.put(kvArr[0], "null");
                                }
                            }
                        }

                        if (valueNull) {
                            beanWrapper.setPropertyValue(reference.getField().getName(), null);
                        } else {
                            List<?> selectList = tableDAO.select(reference.getReferenceClass(), select.value(), params);
                            Object value = Collection.class.isAssignableFrom(reference.getField().getType()) ? selectList : OperatorUtils.expectedAsOptional(selectList).orElse(null);
                            beanWrapper.setPropertyValue(reference.getField().getName(), value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int deleteById(ID id) {
        return delete("id = ?", id);
    }

    @Override
    public int deleteByIds(Collection<ID> ids) {
        return delete("id IN (:ids)", Maps.of("ids", ids));
    }

    @Override
    public int deleteAll() {
        return delete(null);
    }

    @Override
    public int delete(String condition, Object... args) {
        boolean deleteEmpty = false;
        if (hasDeleteReference()) {
            // 级联删除(@OneToMany @ManyToMany)
            List<ID> ids = select(tableMeta.getIdMeta().getIdClass(), tableMeta.getIdMeta().getIdPropertyName(), condition, args);
            deleteReference(ids);
            deleteEmpty = CollectionUtils.isEmpty(ids);
        }

        return deleteEmpty ?  0 : tableDAO.delete(tableMeta.getTableName(), condition, args);
    }

    @Override
    public int delete(String condition, Map<String, ?> paramMap) {
        if (hasDeleteReference()) {
            // 级联删除(@OneToMany @ManyToMany)
            List<ID> ids = select(tableMeta.getIdMeta().getIdClass(), tableMeta.getIdMeta().getIdPropertyName(), condition, paramMap);
            deleteReference(ids);
        }
        return tableDAO.delete(tableMeta.getTableName(), condition, paramMap);
    }

    private boolean hasDeleteReference() {
        return MapUtils.isNotEmpty(tableMeta.getReferenceMap()) && tableMeta.getReferenceMap().values().stream().anyMatch(reference -> (Objects.nonNull(reference.getManyToMany()) && reference.getManyToMany().cascadeDelete()) || (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeDelete()));
    }

    private int deleteReference(List<ID> ids) {
        int row = 0;
        // 级联删除(@OneToMany @ManyToMany)
        if (CollectionUtils.isNotEmpty(ids)) {

            for (Map.Entry<Field, TableMeta.Reference> fieldReferenceEntry : tableMeta.getReferenceMap().entrySet()) {
                TableMeta.Reference reference = fieldReferenceEntry.getValue();
                if (Objects.nonNull(reference.getManyToMany()) || Objects.nonNull(reference.getOneToMany())) {
                    if (Objects.nonNull(reference.getManyToMany()) && reference.getManyToMany().cascadeDelete()) {
                        tableDAO.delete(reference.getManyToMany().tableName(), reference.getManyToMany().joinColumnId()+" IN (:ids)", Maps.of("ids", ids));
                    } else if (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeDelete()) {
                        EntityDAO referenceDAO = EntityDAOManager.getDAO(reference.getReferenceClass());
                        row += referenceDAO.delete(StringUtils.defaultIfBlank(reference.getOneToMany().joinColumnId(), tableMeta.getReferenceColumnId()) + " IN (:ids)", Maps.of("ids", ids));
                    }
                }
            }
        }

        return row;
    }

    @Override
    public Collection<T> insertOrUpdate(Collection<T> entityList) {
        if (CollectionUtils.isNotEmpty(entityList)) {
            for (T t : entityList) {
                insertOrUpdate(t);
            }
        }

        return entityList;
    }

    @Override
    public T insertOrUpdate(T entity) {
        return insertOrUpdate0(entity, Objects.isNull(getIdValue(entity)));
    }

    @Override
    public T insert(T entity) {
        return insertOrUpdate0(entity, true);
    }

    @Override
    public T update(T entity) {
        return insertOrUpdate0(entity, false);
    }

    private T insertOrUpdate0(T entity, boolean insert) {
        if (hasSaveReference()) {
            for (Map.Entry<Field, TableMeta.Reference> fieldReferenceEntry : tableMeta.getReferenceMap().entrySet()) {
                TableMeta.Reference reference = fieldReferenceEntry.getValue();

                if (Objects.nonNull(reference.getManyToOne()) && reference.getManyToOne().cascadeSave()) {
                    EntityDAO referenceDAO = EntityDAOManager.getDAO(reference.getReferenceClass());
                    Object referenceEntity = getPropertyValue(entity, reference.getField().getName());
                    referenceDAO.insertOrUpdate(referenceEntity);
                }
            }
        }

        if (insert) {
            Map<String, Object> args = getArgsFromEntity(entity, false);
            if (Objects.nonNull(tableMeta.getVersionField())) {
                args.put(tableMeta.getFieldColumnNameMap().get(tableMeta.getVersionField()), 1);
            }

            if (Objects.nonNull(getIdValue(entity))) {
                tableDAO.insert(tableMeta.getTableName(), args);
            } else if (tableMeta.getIdMeta().getId().strategy() == Id.GenerationType.SEQUENCE) {
                args.put(tableMeta.getIdMeta().getIdPropertyName(), IdGenerator.getSequenceId());
                tableDAO.insert(tableMeta.getTableName(), args);
                setIdValue(entity, args.get(tableMeta.getIdMeta().getIdPropertyName()));
            } else {
                setIdValue(entity, tableDAO.insertAndReturnKey(tableMeta.getTableName(), args, tableMeta.getIdMeta().getIdPropertyName()));
            }
        } else {
            Map<String, Object> args = getArgsFromEntity(entity, true);
            if (Objects.nonNull(tableMeta.getVersionField())) {
                String versionColumn = tableMeta.getFieldColumnNameMap().get(tableMeta.getVersionField());
                Number version = (Number) getPropertyValue(entity, tableMeta.getVersionField().getName());
                if (Objects.isNull(version)) {
                    throw new IllegalArgumentException("version field cannot be null");
                }

                Number dbVersion = select(Number.class, versionColumn, "id = ?", args.get(tableMeta.getIdMeta().getIdPropertyName())).get(0);
                if (dbVersion.intValue() > version.intValue()) {
                    throw new IllegalArgumentException("version field is old");
                }

                args.put(tableMeta.getVersionField().getName(), dbVersion.intValue() + 1);
            }

            update(tableMeta.getUpdateColumn(), tableMeta.getIdMeta().getIdColumnName() + " = :" + tableMeta.getIdMeta().getIdPropertyName(), args);
        }

        if (hasSaveReference()) {
            for (Map.Entry<Field, TableMeta.Reference> fieldReferenceEntry : tableMeta.getReferenceMap().entrySet()) {
                TableMeta.Reference reference = fieldReferenceEntry.getValue();
                if (Objects.nonNull(reference.getManyToMany()) || Objects.nonNull(reference.getOneToMany())) {
                    EntityDAO referenceDAO = EntityDAOManager.getDAO(reference.getReferenceClass());
                    if (Objects.nonNull(reference.getManyToMany())) {
                        List<Object> list = (List<Object>) getPropertyValue(entity, reference.getField().getName());

                        tableDAO.delete(reference.getManyToMany().tableName(), reference.getManyToMany().joinColumnId()+" = ?", getIdValue(entity));

                        if (CollectionUtils.isNotEmpty(list)) {
                            if (reference.getManyToMany().cascadeSave()) {
                                for (Object referenceEntity : list) {
                                    referenceDAO.insertOrUpdate(referenceEntity);
                                }
                            }

                            for (Object referenceEntity : list) {
                                tableDAO.insert(reference.getManyToMany().tableName(), Maps.of(reference.getManyToMany().joinColumnId(), getIdValue(entity), reference.getManyToMany().inverseJoinColumnId(), getIdValue(referenceEntity)));
                            }
                        }

                    } else if (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeSave()) {
                        List<Object> list = null;
                        if (reference.getOneToMany().oneToOne()) {
                            Object referenceEntity = getPropertyValue(entity, reference.getField().getName());
                            if (Objects.nonNull(referenceEntity)) {
                                list = Arrays.asList(referenceEntity);
                            }
                        } else {
                            list = (List<Object>)getPropertyValue(entity, reference.getField().getName());
                        }

                        String referenceColumnId = StringUtils.defaultIfBlank(reference.getOneToMany().joinColumnId(), tableMeta.getReferenceColumnId());
                        ID idValue = (ID) getIdValue(entity);
                        if (CollectionUtils.isEmpty(list)) {
                            referenceDAO.delete(referenceColumnId + " = ?", idValue);
                        } else {
                            Set<?> ids = list.stream().map(e -> getIdValue(e)).filter(id -> Objects.nonNull(id)).collect(Collectors.toSet());
                            if (CollectionUtils.isEmpty(ids)) {
                                referenceDAO.delete(referenceColumnId + " = ?", idValue);
                            } else {
                                Map<String, Object> paramMap = new LinkedHashMap();
                                paramMap.put("ids", ids);
                                paramMap.put("referenceId", idValue);
                                referenceDAO.delete("id NOT IN (:ids) AND "+ referenceColumnId +" = :referenceId", paramMap);
                            }

                            for (Object referenceEntity : list) {
                                setPropertyValue(referenceEntity, reference.getOneToMany().mappedBy(), entity);
                                referenceDAO.insertOrUpdate(referenceEntity);
                            }
                        }
                    }
                }
            }
        }

        return entity;
    }

    private boolean hasSaveReference() {
        return MapUtils.isNotEmpty(tableMeta.getReferenceMap()) && tableMeta.getReferenceMap().values().stream().anyMatch(reference -> Objects.nonNull(reference.getManyToMany()) || (Objects.nonNull(reference.getOneToMany()) && reference.getOneToMany().cascadeSave()) || (Objects.nonNull(reference.getManyToOne()) && reference.getManyToOne().cascadeSave()));
    }

    @Override
    public int updateById(String columns, ID id, Object... args) {
        return update(columns, "id = :id", appendId(id, args));
    }

    public int updateById(String columns, ID id, Map<String, ?> paramMap) {
        Map<String, Object> args = new LinkedHashMap<>(paramMap);
        args.put("id", id);
        return update(columns, "id = :id", args);
    }

    @Override
    public int updateByIds(String columns, Collection<ID> ids, Map<String, ?> paramMap) {
        Map<String, Object> args = new LinkedHashMap<>(paramMap);
        args.put("ids", ids);
        return update(columns, "id IN (:ids)", args);
    }

    @Override
    public int update(String columns, String condition, Object... args) {
        return tableDAO.update(tableMeta.getTableName(), columns, condition, args);
    }

    @Override
    public int update(String columns, String condition, Map<String, ?> paramMap) {
        return tableDAO.update(tableMeta.getTableName(), appendColumnVar(columns, true), condition, paramMap);
    }

    public Object[] appendId(ID arg, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = arg;
        return newArgs;
    }

    private void setIdValue(Object entity, Object id) {
        setPropertyValue(entity, EntityDAOManager.getDAO(entity.getClass()).getTableMeta().getIdMeta().getIdPropertyName(), id);
    }

    private Object getIdValue(Object entity) {
        if (Objects.isNull(entity)) {
            return null;
        }

        return getPropertyValue(entity, EntityDAOManager.getDAO(entity.getClass()).getTableMeta().getIdMeta().getIdPropertyName());
    }

    private void setPropertyValue(Object bean, String propertyName, Object value) {
        String[] parts = propertyName.split("\\.");
        Object current = bean;
        BeanWrapperImpl wrapper = new BeanWrapperImpl(current);

        try {
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                Object property = wrapper.getPropertyValue(part);

                if (property == null) {
                    Class<?> type = wrapper.getPropertyType(part);
                    if (type == null) {
                        return; // 属性不存在
                    }
                    // 通过无参构造器创建中间对象
                    Object newInstance = type.getDeclaredConstructor().newInstance();
                    wrapper.setPropertyValue(part, newInstance);
                    property = newInstance;
                }

                // 切换 wrapper 到下一级
                current = property;
                wrapper = new BeanWrapperImpl(current);
            }

            // 设置最终属性
            wrapper.setPropertyValue(parts[parts.length - 1], value);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set property: " + propertyName, e);
        }
    }

    private Object getPropertyValue(Object entity, String propertyName) {
        if (Objects.isNull(entity)) {
            return null;
        }
        try {
            return new BeanWrapperImpl(entity).getPropertyValue(propertyName);
        } catch (BeansException exception) {
            return null;
        }
    }

    private Map<String, Object> getArgsFromEntity(T entity, boolean isPropertyName) {
        Map<String, Object> args = new HashMap<>();
        for (Map.Entry<Field, String> entry : tableMeta.getFieldColumnNameMap().entrySet()) {
            args.put(isPropertyName ? tableMeta.getFieldPropertyNameMap().get(entry.getKey()) : entry.getValue(), parsingColumnValue(entry.getKey(), entry.getValue(), getPropertyValue(entity, tableMeta.getFieldPropertyNameMap().get(entry.getKey()))));
        }

        return args;
    }

    private Object parsingColumnValue(Field field, String columnName, Object value) {
        if (Objects.isNull(value)) {
            return null;
        }

        EntityDAO entityDAO = EntityDAOManager.getDAO(value.getClass());
        if (Objects.nonNull(entityDAO)) {
            return getPropertyValue(value, entityDAO.getTableMeta().getIdMeta().getIdPropertyName());
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            return String.valueOf(value);
        }

        return value;
    }

    private String appendColumnVar(String columns, boolean namedVar) {
        String[] columnArr = columns.split(COLUMN_NAME_SEPARATOR_REGEX);
        return Arrays.stream(columnArr).map(column -> namedVar ?  column + " = :" + tableMeta.getColumnPropertyNameMap().get(column) : "?").collect(Collectors.joining(", "));
    }

}
