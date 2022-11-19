package com.tiecode.plugin.autocode.action;

import com.tiecode.develop.plugin.chinese.base.api.compiler.TiecodeCompilerAction;
import com.tiecode.platform.compiler.toolchain.processor.AnnotationProvider;
import com.tiecode.plugin.autocode.processor.CleanUpProcessor;
import com.tiecode.plugin.autocode.processor.CustomComponentProcessor;
import com.tiecode.plugin.autocode.processor.DataClassProcessor;
import com.tiecode.plugin.autocode.processor.ForceDependencyProcessor;
import com.tiecode.plugin.autocode.processor.GenerateCreateProcessor;
import com.tiecode.plugin.autocode.processor.HashObjectProcessor;
import com.tiecode.plugin.autocode.processor.ParameterNameProcessor;
import com.tiecode.plugin.autocode.processor.SerializeClassProcessor;
import com.tiecode.plugin.autocode.processor.SingleInstanceProcessor;
import com.tiecode.plugin.autocode.processor.ToMainPkgProcessor;

public class AutoCodeCompilerAction extends TiecodeCompilerAction {

    @Override
    public void onProvideAnnotation(AnnotationProvider provider) {
        provider.registerAnnotation(DataClassProcessor.DATA_CLASS, new DataClassProcessor());
        provider.registerAnnotation(HashObjectProcessor.HASH_OBJECT, new HashObjectProcessor());
        provider.registerAnnotation(SerializeClassProcessor.SERIALIZE_CLASS, new SerializeClassProcessor());
        provider.registerAnnotation(ParameterNameProcessor.PARAM_NAME, new ParameterNameProcessor());
        provider.registerAnnotation(GenerateCreateProcessor.GENERATE_CREATE, new GenerateCreateProcessor());
        provider.registerAnnotation(SingleInstanceProcessor.SINGLE, new SingleInstanceProcessor());
        provider.registerAnnotation(ToMainPkgProcessor.TO_MAIN_PKG, new ToMainPkgProcessor());
        provider.registerAnnotation(CustomComponentProcessor.CUSTOM_COMPONENT, new CustomComponentProcessor());
        provider.registerAnnotation(CleanUpProcessor.CLEAN_UP, new CleanUpProcessor());
        provider.registerAnnotation(ForceDependencyProcessor.FORCE_DEPENDENCY, new ForceDependencyProcessor());
    }
}
