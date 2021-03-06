//
//  PersistenceHelper.java
//  blogwt
//
//  Created by William Shakour (billy1380) on 13 Apr 2017.
//  Copyright © 2017 WillShex Limited. All rights reserved.
//
package com.willshex.blogwt.server.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;
import com.willshex.blogwt.server.service.ISortable;
import com.willshex.blogwt.server.service.persistence.batch.Batcher.BatchGetter;
import com.willshex.blogwt.shared.api.SortDirectionType;
import com.willshex.blogwt.shared.api.datatype.DataType;
import com.willshex.blogwt.shared.util.LongSparseArray;

/**
 * @author William Shakour (billy1380)
 *
 */
public class PersistenceHelper {

	public static <T> Long keyToId (Key<T> key) {
		return key == null ? null : Long.valueOf(key.getId());
	}

	public static <T> Key<T> idToKey (Class<? extends T> kindClass, Long id) {
		return Key.create(kindClass, id.longValue());
	}

	public static <T> List<Long> keysToIds (Iterable<Key<T>> keys) {
		List<Long> collection = null;
		if (keys != null) {
			collection = new ArrayList<Long>();
			for (Key<T> key : keys) {
				collection.add(keyToId(key));
			}
		}
		return collection;
	}

	public static <T> List<Key<T>> idsToKeys (Class<? extends T> kindClass,
			Collection<Long> ids) {
		List<Key<T>> collection = null;
		if (ids != null) {
			collection = new ArrayList<Key<T>>();
			for (Long id : ids) {
				collection.add(idToKey(kindClass, id));
			}
		}
		return collection;
	}

	public static <T> T type (Class<T> t, Key<T> key, Field f) {
		T i = null;
		if (key != null) {
			try {
				i = t.newInstance();
				if (f.getType() == String.class) {
					f.set(i, key.getName());
				} else if (f.getType() == Long.class) {
					f.set(i, keyToId(key));
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return i;
	}

	public static <T> T type (Class<T> t, Key<T> key) {
		return type(t, key, key(t));
	}

	/**
	 * @param t
	 * @param keys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> typeList (Class<T> t, Iterable<?> ids) {
		List<T> list = null;

		if (ids != null) {
			list = new ArrayList<T>();

			try {
				Field f = key(t);
				T i;
				for (Object id : ids) {
					if (id instanceof Key) {
						list.add(type(t, (Key<T>) id, f));
					} else if (id instanceof Long || id instanceof String) {
						i = (T) t.newInstance();
						f.set(i, id);
						list.add(i);
					}
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public static <LS, T> Map<LS, T> typeMap (Iterable<T> items) {
		Map<LS, T> lookup = new HashMap<>();

		Field f = null;

		for (T item : items) {
			if (f == null) {
				f = key(item.getClass());
			}

			try {
				lookup.put((LS) f.get(item), item);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return lookup;
	}

	public static <T> LongSparseArray<T> typeSparse (Iterable<T> items) {
		LongSparseArray<T> lookup = new LongSparseArray<>();

		Field f = null;

		for (T item : items) {
			if (f == null) {
				f = key(item.getClass());
			}

			try {
				lookup.put(((Long) f.get(item)).longValue(), item);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return lookup;
	}

	/**
	 * @param dataTypeCollection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <LS, T> List<LS> typeCollectionToIds (Iterable<T> iterable) {
		List<LS> ids = null;

		if (iterable != null && iterable.iterator().hasNext()) {
			ids = new ArrayList<>();
			Field f = key(iterable.iterator().next().getClass());

			for (T type : iterable) {
				try {
					ids.add((LS) f.get(type));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return ids;
	}

	public static String direction (SortDirectionType sortDirection) {
		String direction = "";
		if (sortDirection != null) {
			switch (sortDirection) {
			case SortDirectionTypeDescending:
				direction = "-";
				break;
			default:
				break;
			}
		}

		return direction;
	}

	/**
	 * Takes a query and adds pagination parameters
	 * @param filter
	 * @param start
	 * @param count
	 * @param sortBy
	 * @param sortable
	 * @param sortDirection
	 * @return
	 */
	public static <T, E extends Enum<E>> List<T> pagedAndSorted (Query<T> query,
			Integer start, Integer count, E sortBy, ISortable<E> sortable,
			SortDirectionType sortDirection) {
		if (start != null) {
			query = query.offset(start.intValue());
		}

		if (count != null) {
			query = query.limit(count.intValue());
		}

		if (sortBy != null) {
			query = query
					.order(direction(sortDirection) + sortable.map(sortBy));
		}

		return query.list();
	}

	private static <T> Field key (Class<T> c) {
		Field k = null;

		Field[] fs = c.getDeclaredFields();

		for (Field field : fs) {
			Annotation[] as = field.getAnnotations();

			for (Annotation annotation : as) {
				if (annotation instanceof Id) {
					try {
						k = field;
					} catch (IllegalArgumentException ex) {
						throw new RuntimeException(ex);
					}

					break;
				}
			}

			if (k != null) {
				break;
			}
		}

		if (k == null && c.getSuperclass() != Object.class) {
			k = key(c.getSuperclass());
		}

		return k;
	}

	public static <T> T one (Query<T> q) {
		return q.limit(1).first().now();
	}

	/**
	 * Creates a new instance of a type for serialisation with id only
	 * @param c
	 * @param t
	 * @param k
	 * @return
	 */
	public static <T> T slim (Class<T> c, T t, Key<T> k) {
		T id = null;

		try {
			Field f = key(c);
			if (t != null && f != null && f.get(t) != null) {
				id = c.newInstance();
				f.set(id, f.get(t));
			} else if (k != null) {
				id = type(c, k, f);
			} else {
				// something is not right
			}
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}

		return id;
	}

	@SuppressWarnings("unchecked")
	public static <S, T> S id (T t) {
		try {
			return t == null ? null : (S) key(t.getClass()).get(t);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Long id (DataType t) {
		return t.id;
	}

	public static <T> List<T> batchLookupKeys (BatchGetter<T> batcher,
			Iterable<Key<T>> keys) {
		Map<Object, T> map = PersistenceHelper
				.typeMap(batcher.get(PersistenceHelper.keysToIds(keys)));

		return StreamSupport.stream(keys.spliterator(), false).map(i -> {
			return map.get(PersistenceHelper.keyToId(i));
		}).filter(i -> i != null).collect(Collectors.toList());
	}

	/**
	 * @param load
	 * @param id
	 * @return
	 */
	public static <T> T id (LoadType<T> load, Long id) {
		return load.id(id.longValue()).now();
	}
}
