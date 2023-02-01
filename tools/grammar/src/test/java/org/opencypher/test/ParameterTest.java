/*
 * Copyright (c) 2015-2023 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@RunWith(ParameterTest.Runner.class)
public abstract class ParameterTest<PARAMETER>
{
    protected abstract void run( PARAMETER parameter ) throws Throwable;

    public static class Runner extends Suite
    {
        private List<org.junit.runner.Runner> runners;

        public Runner( Class<?> klass ) throws Throwable
        {
            super( klass, emptyList() );
            List<FrameworkMethod> parameters = getTestClass().getAnnotatedMethods( Test.class );
            List<Throwable> errors = null;
            for ( FrameworkMethod parameter : parameters )
            {
                if ( !(parameter.isPublic() && parameter.isStatic()) )
                {
                    if ( errors == null )
                    {
                        errors = new ArrayList<>();
                    }
                    errors.add( new IllegalStateException(
                            "@Test method " + parameter.getName() + " should be public and static" ) );
                }
            }
            if ( errors != null )
            {
                throw new InitializationError( errors );
            }
            this.runners = createRunners( parameters );
        }

        private List<org.junit.runner.Runner> createRunners( List<FrameworkMethod> parameters ) throws Throwable
        {
            List<org.junit.runner.Runner> runners = new ArrayList<>( parameters.size() );
            for ( FrameworkMethod parameter : parameters )
            {
                runners.add( new Single( getTestClass().getJavaClass(), parameter ) );
            }
            return runners;
        }

        @Override
        protected List<org.junit.runner.Runner> getChildren()
        {
            return runners;
        }
    }

    private static class Single extends ParentRunner<Object>
    {
        private final Object parameter;
        private final FrameworkMethod method;
        private final Description description;

        private Single( Class<?> testClass, FrameworkMethod method ) throws Throwable
        {
            super( testClass );
            this.method = method;
            this.parameter = method.invokeExplosively( null );
            this.description = Description.createTestDescription(
                    testClass, method.getName(), method.getAnnotations() );
        }

        @Override
        protected String getName()
        {
            return "[" + parameter + "]";
        }

        @Override
        protected List<Object> getChildren()
        {
            return singletonList( parameter );
        }

        @Override
        protected Description describeChild( Object child )
        {
            return description;
        }

        @Override
        protected void runChild( Object child, RunNotifier notifier )
        {
            if ( method.getAnnotation( Ignore.class ) != null )
            {
                notifier.fireTestIgnored( description );
            }
            else
            {
                runLeaf( statement(), description, notifier );
            }
        }

        private Statement statement()
        {
            ParameterTest test;
            try
            {
                test = (ParameterTest) getTestClass().getOnlyConstructor().newInstance();
            }
            catch ( InvocationTargetException e )
            {
                return new Fail( e.getTargetException() );
            }
            catch ( Throwable e )
            {
                return new Fail( e );
            }
            @SuppressWarnings("unchecked")
            Statement statement = new Invoker( test, parameter );
            statement = possiblyExpectingExceptions( method, statement );
            statement = withBefores( test, statement );
            statement = withAfters( test, statement );
            statement = withTestRules( method, getTestRules( test ), statement );
            return statement;
        }

        /**
         * @see org.junit.runners.BlockJUnit4ClassRunner#withBefores(FrameworkMethod, Object, Statement)
         */
        private Statement withBefores( Object target, Statement statement )
        {
            List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods( Before.class );
            return befores.isEmpty() ? statement : new RunBefores( statement, befores, target );
        }

        /**
         * @see org.junit.runners.BlockJUnit4ClassRunner#withAfters(FrameworkMethod, Object, Statement)
         */
        private Statement withAfters( Object target, Statement statement )
        {
            List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods( After.class );
            return afters.isEmpty() ? statement : new RunAfters( statement, afters, target );
        }

        /**
         * @see org.junit.runners.BlockJUnit4ClassRunner#getTestRules(Object)
         */
        private List<TestRule> getTestRules( Object target )
        {
            List<TestRule> result = getTestClass().getAnnotatedMethodValues( target, Rule.class, TestRule.class );
            result.addAll( getTestClass().getAnnotatedFieldValues( target, Rule.class, TestRule.class ) );
            return result;
        }

        /**
         * @see org.junit.runners.BlockJUnit4ClassRunner#withTestRules(FrameworkMethod, List, Statement)
         */
        private Statement withTestRules( FrameworkMethod method, List<TestRule> testRules, Statement statement )
        {
            return testRules.isEmpty() ? statement : new RunRules( statement, testRules, describeChild( method ) );
        }

        /**
         * @see org.junit.runners.BlockJUnit4ClassRunner#possiblyExpectingExceptions(FrameworkMethod, Object, Statement)
         */
        private Statement possiblyExpectingExceptions( FrameworkMethod method, Statement next )
        {
            Test annotation = method.getAnnotation( Test.class );
            return getExpectedException( annotation ) != null ? new ExpectException(
                    next, getExpectedException( annotation ) ) : next;
        }

        private static Class<? extends Throwable> getExpectedException( Test annotation )
        {
            if ( annotation == null || annotation.expected() == Test.None.class )
            {
                return null;
            }
            else
            {
                return annotation.expected();
            }
        }
    }

    private static class Invoker<P> extends Statement
    {
        private final ParameterTest<P> test;
        private final P parameter;

        Invoker( ParameterTest<P> test, P parameter )
        {
            this.test = test;
            this.parameter = parameter;
        }

        @Override
        public void evaluate() throws Throwable
        {
            test.run( parameter );
        }
    }
}
