package io.advantageous.qbit.meta.builder;


import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.CallType;
import io.advantageous.qbit.meta.ServiceMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static io.advantageous.qbit.meta.builder.ContextMetaBuilder.*;


/**
 * Allows you to build service method data.
 */
@SuppressWarnings("UnusedReturnValue")
public class ServiceMetaBuilder {


    private String name;
    private List<String> requestPaths = new ArrayList<>();
    private List<ServiceMethodMeta> methods = new ArrayList<>();


    private String description;

    public String getDescription() {
        return description;
    }

    public ServiceMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public static ServiceMetaBuilder serviceMetaBuilder() {
        return new ServiceMetaBuilder();
    }

    public String getName() {
        return name;
    }

    public ServiceMetaBuilder setName(String name) {
        this.name = name;
        return this;
    }


    public List<String> getRequestPaths() {
        return requestPaths;
    }

    public ServiceMetaBuilder setRequestPaths(List<String> requestPaths) {
        this.requestPaths = requestPaths;
        return this;
    }


    public ServiceMetaBuilder addRequestPath(String requestPath) {
        this.requestPaths.add(requestPath);
        return this;
    }


    public List<ServiceMethodMeta> getMethods() {
        return methods;
    }

    public ServiceMetaBuilder setMethods(List<ServiceMethodMeta> methods) {
        this.methods = methods;
        return this;
    }


    public ServiceMetaBuilder addMethod(ServiceMethodMeta method) {
        this.methods.add(method);
        return this;
    }


    public ServiceMetaBuilder addMethods(ServiceMethodMeta... methodArray) {
        Collections.addAll(methods, methodArray);
        return this;
    }

    public ServiceMeta build() {

        return new ServiceMeta(getName(), getRequestPaths(), getMethods(), getDescription());
    }



    public void addMethods(String path, Collection<MethodAccess> methods) {
        methods.stream().filter(methodAccess -> !methodAccess.isPrivate()
                && !methodAccess.isStatic())
                .forEach(methodAccess -> addMethod(path, methodAccess));
    }

    public ServiceMetaBuilder addMethod(final String rootPath, final MethodAccess methodAccess) {

        for (String servicePath : this.getRequestPaths()) {


            final List<String> requestPaths
                    = getRequestPathsByAnnotated(methodAccess, methodAccess.name().toLowerCase());

            final String description = getDescriptionFromRequestMapping(methodAccess);

            final String returnDescription = getReturnDescriptionFromRequestMapping(methodAccess);

            final String summary = getSummaryFromRequestMapping(methodAccess);

            final List<RequestMethod> requestMethods = getRequestMethodsByAnnotated(methodAccess);


            ServiceMethodMetaBuilder serviceMethodMetaBuilder = ServiceMethodMetaBuilder.serviceMethodMetaBuilder();
            serviceMethodMetaBuilder.setMethodAccess(methodAccess);
            serviceMethodMetaBuilder.setDescription(description);
            serviceMethodMetaBuilder.setSummary(summary);
            serviceMethodMetaBuilder.setReturnDescription(returnDescription);

            for (String path : requestPaths) {
                CallType callType = path.contains("{") ? CallType.ADDRESS_WITH_PATH_PARAMS : CallType.ADDRESS;

                RequestMetaBuilder requestMetaBuilder = new RequestMetaBuilder();


                requestMetaBuilder.addParameters(rootPath, servicePath, path, methodAccess);
                requestMetaBuilder.setCallType(callType).setRequestURI(path).setRequestMethods(requestMethods);
                serviceMethodMetaBuilder.addRequestEndpoint(requestMetaBuilder.build());
            }
            addMethod(serviceMethodMetaBuilder.build());
        }
        return this;


    }


}
