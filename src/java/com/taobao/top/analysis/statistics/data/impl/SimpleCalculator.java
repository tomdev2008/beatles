package com.taobao.top.analysis.statistics.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.taobao.top.analysis.exception.AnalysisException;
import com.taobao.top.analysis.statistics.data.Alias;
import com.taobao.top.analysis.statistics.data.ICalculator;
import com.taobao.top.analysis.util.AnalysisConstants;
import com.taobao.top.analysis.util.ReportUtil;

public class SimpleCalculator implements ICalculator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8024981401877328739L;
	/**
	 * value表达式中的变量列表
	 */
	private List<Object> bindingStack;
	/**
	 * value表达式中的操作列表
	 */
	private List<Byte> operatorStack;
	
	private final String value;
	
	public String getValue() {
		return value;
	}

	public List<Object> getBindingStack() {
		return bindingStack;
	}

	public List<Byte> getOperatorStack() {
		return operatorStack;
	}
	
	public void init(Map<String, Alias> aliasPool)  throws AnalysisException{


		if (value != null
				&& !"".equals(value)
				&& (value.indexOf("$") >= 0 || value
						.indexOf("entry(") >= 0)) {

			bindingStack = new ArrayList<Object>();
			operatorStack = new ArrayList<Byte>();

			String c = value;
			String temp;

			while (c.indexOf("$") >= 0 || c.indexOf("#") >= 0) {
				if (c.indexOf("$") >= 0) {
					if (c.indexOf("#") < 0
							|| (c.indexOf("#") >= 0 && c.indexOf("$") < c
									.indexOf("#"))) {
						c = c.substring(c.indexOf("$") + 1);
						temp = c.substring(0, c.indexOf("$"));
						c = c.substring(c.indexOf("$") + 1);

						if (aliasPool != null && aliasPool.size() > 0
								&& aliasPool.get(temp) != null) {
							bindingStack.add(aliasPool.get(temp).getKey());
						} else
							bindingStack.add(temp);

						continue;
					}
				}

				if (c.indexOf("#") >= 0) {
					if (c.indexOf("$") < 0
							|| (c.indexOf("$") >= 0 && c.indexOf("$") > c
									.indexOf("#"))) {
						c = c.substring(c.indexOf("#") + 1);
						temp = c.substring(0, c.indexOf("#"));
						c = c.substring(c.indexOf("#") + 1);

						bindingStack.add("#" + temp);

						continue;
					}
				}

			}

			while (c.indexOf("entry(") >= 0) {
				c = c.substring(c.indexOf("entry(") + "entry(".length());
				temp = c.substring(0, c.indexOf(")"));
				c = c.substring(c.indexOf(")") + 1);
				bindingStack.add(temp);
			}

			char[] cs = value.toCharArray();

			for (char _ch : cs) {
				if (_ch == '+' || _ch == '-' || _ch == '*' || _ch == '/')
					operatorStack.add(ReportUtil.generateOperationFlag(_ch));
			}

		}
	
	}


	public SimpleCalculator(String valueExpression,
			Map<String, Alias> aliasPool) throws AnalysisException {
		this.value = valueExpression;
		init(aliasPool);
	}

	@Override
	public Object calculator(Object[] contents) {


		Object result = null;

		double left = 0;

		if (bindingStack != null
				&& bindingStack.size() > 0) {
			if (bindingStack.size() > 1) {
				if (bindingStack.get(0) instanceof String && ((String)bindingStack.get(0)).startsWith("#"))
					left = Double.valueOf(((String)bindingStack.get(0)).substring(1));
				else {
					if ((Integer)bindingStack.get(0) - 1 >= contents.length)
						return result;
					Object o = contents[(Integer)bindingStack.get(0) - 1];
					if(o instanceof Number){
						left = ((Number) o).doubleValue();
					}else{
						left = Double.valueOf(o.toString());
					}
					
				}

				double right = 0;

				int size = bindingStack.size();

				for (int i = 0; i < size - 1; i++) {
					if (bindingStack.get(i + 1) instanceof String && ((String)bindingStack.get(i + 1)).startsWith("#"))
						right = Double.valueOf(((String)bindingStack.get(i + 1))
								.substring(1));
					else {
						if ((Integer)bindingStack.get(i + 1) - 1 >= contents.length)
							return result;
						Object o = contents[(Integer)bindingStack.get(i + 1) - 1];
						if(o instanceof Number){
							right = ((Number) o).doubleValue();
						}else{
							right = Double.valueOf(o.toString());
						}
					}

					if (operatorStack.get(i) == AnalysisConstants.OPERATE_PLUS)
					{
						left += right;
						continue;
					}

					if (operatorStack.get(i) == AnalysisConstants.OPERATE_MINUS)
					{
						left -= right;
						continue;
					}

					if (operatorStack.get(i) == AnalysisConstants.OPERATE_RIDE)
					{
						left = left * right;
						continue;
					}

					if (operatorStack.get(i) == AnalysisConstants.OPERATE_DIVIDE)
					{
						left = left / right;
						continue;
					}

				}

				result = left;
			} else {
				if (bindingStack.get(0) instanceof String && ((String)bindingStack.get(0)).startsWith("#"))
					result = Double.valueOf(((String)bindingStack.get(0)).substring(1));
				else {
					if ((Integer)bindingStack.get(0) - 1 >= contents.length)
						return result;

					result = contents[(Integer)bindingStack.get(0) - 1];
				}

			}

		}

		return result;
	}

}
