package shuai.bao.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import shuai.bao.Domain.Function;

public class CriteriaUtils<T> {
	
	private T model;
	/**
	 * @param String 若是id结尾的则等于判断.若不是则%like%判断
	 * @param map  件为实体类的属性名称  ,  值为">",">=","<","<="
	 * @param 数字只支持 int 和 long  若不写map则为等于
	 * @param 若提供了map 则按map的判断  若写错了则按等于判断
	 * @throws 若 第一个参数model1 提供了一个null 会报出空指针异常
	 * @return  DetachedCriteria
	 */
	/* 1.获得提交参数中的name数组,并判断value不为空同时不为""的name的名字
	 * 2.根据模型驱动获得的model  调用它的get+Name判断他是否为空
	 * 3.若不为空则判断他是否为String类型,若是String类型则调用like方法
	 * 4.若不是String类型,则判断是否为Long 或 interger类型,若是则判断他是否为0.不是则是调用eq方法
	 * 5.若不是String  Long Interger.则调用eq方法
	 */
	
	
	public DetachedCriteria getCriteria(T model1,Map<String, String> criteria){
		 	model = (T) model1;
		 	Map<String, String> map = new HashMap<>();
		 	if(criteria!=null||criteria.length!=0){
		 		 map = criteria;
		 	}
			DetachedCriteria Criteria = DetachedCriteria.forClass(model.getClass());
			//1.获得提交参数中的name数组,并判断value不为空同时不为""的name的名字
			List<String> names = getNames();
	        //2.根据模型驱动获得的model  调用它的get+Name判断他是否为空
			return getParam(names , Criteria , map);
		}
	 
	 private List<String> getNames(){
		  //在程序运行时 获得当前类的getModel方法对象  
		List<String> list = new ArrayList<>();
		try {
       Class<? extends Object> cl = model.getClass();  
       //通过类类型获得 类中的属性对象数组  
       Field[] fi = cl.getDeclaredFields();  
       //通过request获得所有的 表单中提交的name值   
       Enumeration<?> em = ServletActionContext.getRequest().getParameterNames();  
       //循环枚举中的值  
       while(em.hasMoreElements()){  
           //获得枚举中的值  就是表单提交的name值  
           String fieldName = em.nextElement().toString();  
           String parameter = ServletActionContext.getRequest().getParameter(fieldName);
           if(StringUtils.isNotBlank(fieldName)&&StringUtils.isNotBlank(parameter)){
	            //循环类中所有的属性对象  
           	String[] split = fieldName.split("[.]");
	            for(int i =0;i<fi.length;i++){
	                //判断 如果类中属性的名字 和 表单中提交的名字一致  
	                if(split[0].equals(fi[i].getName())){  
	                	list.add(fieldName);
	                	break;
	                		}  
	            		}  
	        		}
           	}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return list;
	}
	
	 
	private DetachedCriteria getParam(List<String> names,DetachedCriteria Criteria ,Map<String, String> map){
		Map<String , Object> param = new HashMap<String , Object>();
		for (String name : names) {
			Method method = null;
			Object obj  = model;
			Object param1 = null;
			String string = "";
			String[] split = name.split("[.]");
			for (int i = 0 ; i < split.length ; i++) {
				string = split[i];
				Class clazz = obj.getClass();
				String substring = string.substring(0, 1);
				String upperCase = substring.toUpperCase();
				String substring2 = string.substring(1);
				string = upperCase + substring2;
				string = "get" + string;
				try {
					method = clazz.getMethod(string, null);
					Object invoke = method.invoke(obj, null);
					if(invoke!=null){
						obj = invoke;
						if(i==0){
							param1 = invoke;
						}
					}else{
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			if(obj!=null&&obj instanceof String&&!obj.equals("")){
				if((string.endsWith("id")||string.endsWith("Id")||string.endsWith("code")||string.endsWith("Code"))&&!obj.equals("0")){
					Criteria.add(Restrictions.eq(split[0], param1));
					continue;
				}else{
				Criteria.add(Restrictions.like(name, "%"+obj+"%"));}
			}else{
				Integer  i = 0;
				Long  l = 0L;
				Double d = 0.0;
				try {
					i =   (Integer)obj;
					if(i!=0){
						Criteria = add(Criteria, split[0], param1, map);
					}
				} catch (Exception e) {
					try {
						l =   (Long)obj;
						if(l!=0L){
							Criteria = add(Criteria, split[0], param1, map);
						}
					} catch (Exception e2) {
						try {
							d = (Double)obj;
								Criteria = add(Criteria, split[0], param1, map);
						} catch (Exception e3) {
							
						}
					}
				}
			}
		}
		return Criteria;
	}
	 
	 private DetachedCriteria add(DetachedCriteria Criteria , String name , Object param , Map<String, String> map){
		 String string = map.get(name);
		 if(StringUtils.isNotBlank(string)){
			 if(string.equals(">")){
				 Criteria.add(Restrictions.gt(name, param));
			 }else if(string.equals(">=")){
				 Criteria.add(Restrictions.ge(name, param));
			 }else if(string.equals("<")){
				 Criteria.add(Restrictions.lt(name, param));
			 }
			 else if(string.equals("<=")){
				 Criteria.add(Restrictions.le(name, param));
			 }else{
				 Criteria.add(Restrictions.eq(name, param));
			 }
		 }else{
			 	Criteria.add(Restrictions.eq(name, param));
		 }
		 return Criteria;
	 }


}
