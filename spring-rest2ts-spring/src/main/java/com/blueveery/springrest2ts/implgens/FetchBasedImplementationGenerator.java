package com.blueveery.springrest2ts.implgens;

import com.blueveery.springrest2ts.converters.TypeMapper;
import com.blueveery.springrest2ts.tsmodel.*;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import static com.blueveery.springrest2ts.spring.RequestMappingUtility.getRequestMapping;

public class FetchBasedImplementationGenerator extends BaseImplementationGenerator {

    private SortedSet<TSField> implementationSpecificFieldsSet = new TreeSet<>();
    private TSField baseUrlTsField;

    @Override
    public void write(BufferedWriter writer, TSMethod method) throws IOException {
        if (method.isConstructor()) {
            for (TSField field : implementationSpecificFieldsSet) {
                writer.write("this." + field.getName() + " = " + field.getName() + ";");
            }
        } else {
            RequestMapping methodRequestMapping = getRequestMapping(method.getAnnotationList());
            RequestMapping classRequestMapping = getRequestMapping(method.getOwner().getAnnotationList());

            String tsPath = getPathFromRequestMapping(classRequestMapping) + getPathFromRequestMapping(methodRequestMapping) + "'";
            String httpMethod = methodRequestMapping.method()[0].toString();

            String requestUrlVar = "url";
            String requestBodyVar = "body";
            String requestParamsVar = "url.searchParams";

            StringBuilder pathStringBuilder = new StringBuilder(tsPath);
            StringBuilder requestBodyBuilder = new StringBuilder();
            StringBuilder requestParamsBuilder = new StringBuilder();

            assignMethodParameters(method, requestParamsVar, pathStringBuilder, requestBodyBuilder, requestParamsBuilder);
            writeRequestUrl(writer, requestUrlVar, pathStringBuilder);

            boolean isRequestBodyDefined = !isStringBuilderEmpty(requestBodyBuilder);
            if (isRequestBodyDefined) {
                requestBodyVar = requestBodyBuilder.toString();
            }

            writer.write(requestParamsBuilder.toString());
            writer.newLine();

            String requestOptions = composeRequestOptions(requestBodyVar, isRequestBodyDefined, httpMethod, methodRequestMapping.consumes());

            writer.write(
                    "return fetch(" + requestUrlVar + ".toString(), {"
                            + "method: '" + httpMethod + (requestOptions.isEmpty() ? "'" : "',")
                            + requestOptions
                            + "})" + getContentFromResponseFunction(method) + ";");
        }

    }

    private void writeRequestUrl(BufferedWriter writer, String requestUrlVar, StringBuilder pathStringBuilder) throws IOException {
        String tsPath = pathStringBuilder.toString();
        tsPath = tsPath.startsWith("/") ? tsPath : "/" + tsPath;
        writer.write("const " + requestUrlVar + " = " + " new URL('" + tsPath + ", this." + this.baseUrlTsField.getName() + ");");
        writer.newLine();
    }

    private String getContentFromResponseFunction(TSMethod method) {
        TSType actualType = method.getType();

        String parseFunction = "";
        if (actualType == TypeMapper.tsNumber) {
            parseFunction = "res.text()).then(res => Number(res)";
        } else if (actualType == TypeMapper.tsBoolean) {
            parseFunction = "res.text()).then(res => (res === 'true')";
        } else if (actualType == TypeMapper.tsString) {
            parseFunction = "res.text()";
        } else if (actualType == TypeMapper.tsVoid) {
            return "";
        } else {
            parseFunction = "res.json()";
        }
        return ".then(res =>  " + parseFunction + ")";
    }

    protected void initializeHttpParams(StringBuilder requestParamsBuilder) {

    }

    protected void addRequestParameter(StringBuilder requestParamsBuilder, String requestParamsVar, TSParameter tsParameter, String requestParamName) {
        String tsParameterName = callToStringOnParameterIfRequired(tsParameter);
        requestParamsBuilder
                .append("\n")
                .append(requestParamsVar)
                .append(".append('")
                .append(requestParamName)
                .append("',").append(tsParameterName)
                .append(");");
    }


    private String composeRequestOptions(String requestBodyVar, boolean isRequestBodyDefined, String httpMethod, String[] consumesContentType) {
        String requestOptions = "";
        List<String> requestOptionsList = new ArrayList<>();
        if (("PUT".equals(httpMethod) || "POST".equals(httpMethod)) && isRequestBodyDefined) {
            addContentTypeHeader(consumesContentType, requestOptionsList);
            requestOptionsList.add("body: JSON.stringify(" + requestBodyVar + ")");
        }

        requestOptions += String.join(", ", requestOptionsList);
        return requestOptions;
    }

    private void addContentTypeHeader(String[] consumesContentType, List<String> requestOptionsList) {
        String contentType = getConsumesContentType(consumesContentType);
        String headers = "headers: {";
        headers += "'Content-Type': '" + contentType + "'";
        headers += "}";
        requestOptionsList.add(headers);
    }

    @Override
    public TSType mapReturnType(TSMethod tsMethod, TSType tsType) {
        if (isRestClass(tsMethod.getOwner())) {
            if (tsType == TypeMapper.tsVoid) {
                return new TSParameterisedType("", new TSInterface("Promise", null), new TSInterface("Response", null));
            }
            return new TSParameterisedType("", new TSInterface("Promise", null), tsType);
        }
        return tsType;
    }

    @Override
    public SortedSet<TSField> getImplementationSpecificFields(TSComplexType tsComplexType) {
        if (isRestClass(tsComplexType)) {
            return implementationSpecificFieldsSet;
        }
        return Collections.emptySortedSet();
    }

    @Override
    public List<TSParameter> getImplementationSpecificParameters(TSMethod method) {
        if (method.isConstructor() && isRestClass(method.getOwner())) {
            List<TSParameter> tsParameters = new ArrayList<>();
            TSParameter newParameter = new TSParameter(baseUrlTsField.getName(), baseUrlTsField.getType(), this, "new URL(window.document.URL)");
            tsParameters.add(newParameter);
            return tsParameters;
        }
        return Collections.emptyList();
    }

    @Override
    public List<TSDecorator> getDecorators(TSMethod tsMethod) {
        return Collections.emptyList();
    }

    @Override
    public List<TSDecorator> getDecorators(TSClass tsClass) {
        return Collections.emptyList();
    }

    @Override
    public void addComplexTypeUsage(TSClass tsClass) {

    }

    @Override
    public void addImplementationSpecificFields(TSComplexType tsComplexType) {
        baseUrlTsField = new TSField("baseURL", tsComplexType, new TSInterface("URL", TypeMapper.systemModule));
        implementationSpecificFieldsSet.add(baseUrlTsField);
    }

    private boolean isRestClass(TSComplexType tsComplexType) {
        return tsComplexType.findAnnotation(RequestMapping.class) != null;
    }

}
