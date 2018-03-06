package ${package};

<#list importPackage as import>
    <#if import??>
import ${import!""};
    </#if>
</#list>

public class ${fileName} {

<#list models as model>
    <#if (model.columnKey=="PRI")>
    @Id
    </#if>
    private ${model.dataType!""}  ${model.columnName!""};
</#list>

<#list models as model>
    public ${model.dataType!""} get${model.columnName?cap_first}() {
        return ${model.columnName!""};
    }
    public void set${model.columnName?cap_first}(${model.dataType!""} ${model.columnName!""}) {
        this.${model.columnName!""} = ${model.columnName!""};
    }
</#list>

}