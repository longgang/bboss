package com.frameworkset.common.hibernate.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.framework.orm.ObjectRetrievalFailureException;
import org.framework.orm.hibernate3.support.HibernateDaoSupport;

import com.frameworkset.util.ListInfo;

/**
 * <p>Title: BaseDAOHibernate</p>
 *
 * <p>Description: dao�Ĺ������࣬�ṩ��һЩ���õ����ݿ��ѯ</p>
 *

 *
 * <p>
 * bboss workgroup
 * </p>
 * <p>
 * Copyright (c) 2007
 * </p>
 * 
 * @Date 2009-6-1 ����08:58:51
 * @author biaoping.yin
 * @version 1.0
 */
public class BaseDAOHibernate
    extends HibernateDaoSupport 
	implements DAO,Serializable
{
	private static Logger log = Logger.getLogger(BaseDAOHibernate.class);
	public BaseDAOHibernate()
	{

	}

	public void removeObject(Object o)
		throws DataAccessException
	{
		getHibernateTemplate().delete(o);
	}

	public Object getObject(Class clazz, Serializable id)
		throws DataAccessException
	{
		Object o = getHibernateTemplate().get(clazz, id);
		if(o == null)
			throw new ObjectRetrievalFailureException(clazz, id);
		else
			return o;
	}

	public List getObjects(Class clazz)
		throws DataAccessException
	{
		return getHibernateTemplate().loadAll(clazz);
	}

	public void removeObject(Class clazz, Serializable id)
		throws DataAccessException
	{
		getHibernateTemplate().delete(getObject(clazz, id));
	}

	public void saveObject(Object o)
		throws DataAccessException
	{
		getHibernateTemplate().save(o);
	}

	public void updateObject(Object o)
		throws DataAccessException
	{
		getHibernateTemplate().update(o);
	}

	/**
	 * ��ѯ����Ϊclazz�����м�¼
	 * @param clazz Class
	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 */
	public ListInfo getObjects(Class clazz,long start,int maxSize)
		throws DataAccessException
	{

		return find("from " + clazz.getName(), start,maxSize);
	}


	/**
	 * ִ��Ԥ�����ҳ��ѯ�����ҷ��ز�ѯ�������صķ�ҳ��Ϣ���ܼ�¼����
	 * @param sql String Ԥ�����ѯ���
	 * @param objs Object[] Ԥ�����ѯ����������
	 * @param types Type[] Ԥ�����ѯ��������������
 	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 * @deprecated ����������hibernate 2.x���ݶ�д������ķ���Ϊprotected List find(String sql, Object objs[], Pagination pagination)
	 */
	public ListInfo find(String sql, Object objs[], Type types[], long start,int maxSize)
		throws DataAccessException
	{
		return find(sql, objs, start,maxSize);
	}

	/**
	 * ִ��Ԥ�����ҳ��ѯ�����ҷ��ز�ѯ�������صķ�ҳ��Ϣ���ܼ�¼����
	 * @param sql String Ԥ�����ѯ���
	 * @param objs Object[] Ԥ�����ѯ����������
	 * @param types Type[] Ԥ�����ѯ��������������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 * @deprecated ����������hibernate 2.x���ݶ�д������ķ���Ϊprotected List find(String sql, Object objs[])
	 */
	public List find(String sql, Object objs[], Type types[])
		throws DataAccessException
	{
		return super.getHibernateTemplate().find(sql, objs);
	}

	/**
	 * ִ��Ԥ�����ҳ��ѯ�����ҷ��ز�ѯ�������صķ�ҳ��Ϣ���ܼ�¼����
	 * @param sql String Ԥ�����ѯ���
	 * @param objs Object[] Ԥ�����ѯ����������
	 * @param types Type[] Ԥ�����ѯ��������������
	 *
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 * @deprecated ����������hibernate 2.x���ݶ�д������ķ���Ϊprotected List find(String sql, Object objs[])
	 */
	public List find(String sql, Object objs[])
		throws DataAccessException
	{
		return super.getHibernateTemplate().find(sql, objs);
	}


	/**
	 * ִ��Ԥ�����ҳ��ѯ�����ҷ��ز�ѯ�������صķ�ҳ��Ϣ���ܼ�¼����
	 * @param sql String Ԥ�����ѯ���
	 * @param objs Object[] Ԥ�����ѯ����������
	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 */
	public ListInfo find(String sql, Object objs[],long start,int maxSize)
		throws DataAccessException
	{
		ListInfo listInfo = new ListInfo();
		listInfo.setTotalSize(loadTotalSize(sql, objs));
		Session session = getSession();
		Query sqlQuery = null;
		List ls = null;
		try
		{
			sqlQuery = session.createQuery(sql);
			if(objs != null && objs.length > 0 )
			{
				for(int i = 0; i < objs.length; i++)
					sqlQuery.setParameter(i, objs[i]);

			}
			sqlQuery.setFirstResult((int)start).setMaxResults(maxSize);
			ls = sqlQuery.list();
			if(ls == null)
				ls = new ArrayList(0);
		}
		catch(HibernateException e)
		{
			log.error(e);
		}
		listInfo.setDatas(ls);
		return listInfo;
	}

	/**
	 * ִ�з�ҳ��ѯ
	 * @param sql String ��ѯ���
	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 */
	public ListInfo find(String sql, long start,int maxSize)
		throws DataAccessException
	{
		return find(sql, new Object[0], start, maxSize);
	}

	/**
	 * ִ�в�ѯ
	 * @param sql String ��ѯ���
	 * @return List ����б�
	 * @throws DataAccessException
	 */
	public List find(String sql)
		throws DataAccessException
	{
		return getHibernateTemplate().find(sql);
	}


	/**
	 * ִ��Ԥ�����ѯ��䣬���ؽ���б�
	 * @param sql String Ԥ�������
	 * @param obj Object Ԥ�����ѯ����ֵ
	 * @param type Type Ԥ�����ѯ��������
	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 * @deprecated ������Ϊ��hibernate 2.x���ݶ���Ƶ�,hibernate 3.x����ķ���Ϊprotected List find(String sql, Object obj, Pagination pagination)
	 */
	public ListInfo find(String sql, Object obj, Type type, long start, int maxSize)
		throws DataAccessException
	{
		return find(sql, obj, start,maxSize);
	}

	/**
	 * ִ��Ԥ�����ѯ��䣬���ؽ���б�
	 * @param sql String Ԥ�������
	 * @param obj Object Ԥ�����ѯ����ֵ
	 * @param start ��ҳ������ʼλ��
 	 * @param maxSize ��ȡ��¼�������
	 * @return ListInfo ��װ����б����ܼ�¼��
	 * @throws DataAccessException
	 */
	public ListInfo find(String sql, Object obj,long start, int maxSize)
		throws DataAccessException
	{
			return find(sql, new Object[] {
				obj
			}, start,maxSize);
	}



	/**
	 * ִ��Ԥ�����ѯ��䣬���ؽ���б�
	 * @param sql String Ԥ�������
	 * @param obj Object Ԥ�����ѯ����ֵ
	 * @return List ����б�
	 * @throws DataAccessException
	 */
	public List find(String sql, Object obj)
		throws DataAccessException
	{
		return getHibernateTemplate().find(sql, obj);
	}



	/**
	 * ִ��Ԥ����sql��䣬��ȡ��ѯ������ܼ�¼����objs[]�����Ų�ѯ����
	 * @param sql String
	 * @param objs Object[]
	 * @return long
	 * @throws DataAccessException
	 */
	public long loadTotalSize(String sql, Object objs[])
		throws DataAccessException
	{
		long count = 0L;
		try
		{
			String midSql = getCountSql(sql);
			midSql = "select count(*) " + midSql;

			//hibernate 3.x �ж�Ӧ�ķ���
			List ls = getHibernateTemplate().find(midSql, objs);
			if(ls != null && ls.size() > 0)
			{
				Object obj = ls.get(0);
				if(obj instanceof Integer)
					count = ((Integer)obj).longValue();
				else
				if(obj instanceof Long)
					count = ((Long)obj).longValue();
			}
		}
		catch(Exception he)
		{
			log.error(he.getMessage(), he);
		}
		return count;
	}

	/**
	 * ��ȡ��¼����
	 * ��hibernate 2.x���ݵķ���
	 * @param sql String
	 * @param objs Object[]
	 * @param types Type[]
	 * @return long
	 * @throws DataAccessException
	 * @deprecated ����ķ���Ϊprotected long loadTotalSize(String sql, Object objs[])
	 */
	public long loadTotalSize(String sql, Object objs[], Type types[])
		throws DataAccessException
	{
		return loadTotalSize(sql, objs);
	}

	/**
	 * ��ȡhql�е�from�Ӿ�
	 * @param sql String
	 * @return String
	 */
	public String getCountSql(String sql)
	{
		String midSql = sql;
		int count = StringUtils.indexOf(midSql.toLowerCase(), "from");
		midSql = StringUtils.substring(midSql, count);
		return midSql;
	}

	/**
	 * ��������
	 * @param objs Collection
	 * @return Object
	 */
	public void batchInsert(Collection objs)
	{
		getHibernateTemplate().saveOrUpdateAll(objs);
	}

	/**
	 * ��������
	 * @param objs Collection
	 */
	public void batchUpdate(Collection objs)
	{
		getHibernateTemplate().saveOrUpdateAll(objs);
	}

	/**
	 * ����ɾ��
	 * @param objs Collection
	 */
	public void batchDelete(Collection objs)
	{
		getHibernateTemplate().deleteAll(objs);
	}
}
