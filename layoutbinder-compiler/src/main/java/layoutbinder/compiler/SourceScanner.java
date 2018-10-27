/*
 * Copyright 2018 yinpinjiu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package layoutbinder.compiler;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/**
 * https://stackoverflow.com/questions/43477024/get-android-r-class-when-passing-value-with-annotation
 */
public final class SourceScanner extends TreePathScanner<String, Object> {

    private static final String VARIABLE_VALUE = "value";

    @Override
    public String visitAnnotation(AnnotationTree annotationTree, Object o) {
        List<? extends ExpressionTree> expressionTrees = annotationTree.getArguments();
        AssignmentTree valueAssignmentTree = null;
        for (ExpressionTree expressionTree : expressionTrees) {
            if (expressionTree.getKind() == Tree.Kind.ASSIGNMENT) {
                AssignmentTree assignmentTree = (AssignmentTree) expressionTree;
                if (VARIABLE_VALUE.equals(assignmentTree.getVariable().toString())) {
                    valueAssignmentTree = assignmentTree;
                    break;
                }
            }
        }
        if (valueAssignmentTree == null) return null;
        return valueAssignmentTree.accept(
                new SimpleTreeVisitor<String, Object>() {
                    @Override
                    public String visitAssignment(AssignmentTree assignmentTree, Object o) {
                        return assignmentTree.getExpression().toString();
                    }
                },
                null);
    }

    @Override
    public String visitAnnotatedType(AnnotatedTypeTree annotatedTypeTree, Object o) {
        annotatedTypeTree.getAnnotations();
        return super.visitAnnotatedType(annotatedTypeTree, o);
    }

    public static final String getLayoutId(Trees trees, TypeElement element) {
        AnnotationMirror annotationMirror = element.getAnnotationMirrors().get(0);
        TreePath treePath = trees.getPath(element, annotationMirror);
        return new SourceScanner().scan(treePath, null);
    }
}
