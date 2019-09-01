package org.sourav.lucDeb.resources;

public class InputParam {
	
	String index; 
	String term;
	String fieldName;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getIndex()
	{
		return index;
	}
	public void setIndex(String index)
	{
		this.index = index;
	}
	public String getHaru()
	{
		return term;
	}
	public void setTerm(String term)
	{
		this.term = term;
	}
	
	
	public String toString()
	{
		return "Index : "+ index + "Term : "+ term + "FieldName: " + fieldName;
	}
}
