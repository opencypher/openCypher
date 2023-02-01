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
package org.opencypher.grammar;

import java.util.Arrays;
import java.util.Objects;

public abstract class Conditional
{
    public static final String XML_NAMESPACE = "http://opencypher.org/conditional";

    public interface Flags
    {
        boolean isTrue( String flag );
    }

    public abstract boolean check( Flags flags );

    static final Conditional NONE = new Conditional()
    {
        @Override
        public boolean check( Flags flags )
        {
            return true;
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            return new SingleGiven( flag );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            return new SingleUnless( flag );
        }
    };
    private static final Conditional UNSATISFIABLE = new Conditional()
    {
        @Override
        public boolean check( Flags flags )
        {
            return false;
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            return this;
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            return this;
        }
    };

    abstract Conditional given( String flag );

    abstract Conditional unless( String flag );

    private static class SingleGiven extends Conditional
    {
        private final String flag;

        SingleGiven( String flag )
        {
            this.flag = flag;
        }

        @Override
        public boolean check( Flags flags )
        {
            return flags.isTrue( flag );
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( this.flag.equals( flag ) )
            {
                return this;
            }
            return new MultipleGiven( this.flag, flag );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( this.flag.equals( flag ) )
            {
                return UNSATISFIABLE;
            }
            return new SingleCombined( this.flag, flag );
        }
    }

    private static class SingleUnless extends Conditional
    {
        private final String flag;

        SingleUnless( String flag )
        {
            this.flag = flag;
        }

        @Override
        public boolean check( Flags flags )
        {
            return !flags.isTrue( flag );
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( this.flag.equals( flag ) )
            {
                return UNSATISFIABLE;
            }
            return new SingleCombined( flag, this.flag );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( this.flag.equals( flag ) )
            {
                return this;
            }
            return new MultipleUnless( this.flag, flag );
        }
    }

    private static class MultipleGiven extends Conditional
    {
        private final String[] flags;

        MultipleGiven( String... flags )
        {
            this.flags = flags;
        }

        @Override
        public boolean check( Flags flags )
        {
            for ( String flag : this.flags )
            {
                if ( !flags.isTrue( flag ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : flags )
            {
                if ( f.equals( flag ) )
                {
                    return this;
                }
            }
            String[] flags = Arrays.copyOf( this.flags, this.flags.length + 1 );
            flags[this.flags.length] = flag;
            return new MultipleGiven( flags );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : flags )
            {
                if ( f.equals( flag ) )
                {
                    return UNSATISFIABLE;
                }
            }
            return new Combined( flags, new String[]{flag} );
        }
    }

    private static class MultipleUnless extends Conditional
    {
        private final String[] flags;

        MultipleUnless( String... flags )
        {
            this.flags = flags;
        }

        @Override
        public boolean check( Flags flags )
        {
            for ( String flag : this.flags )
            {
                if ( flags.isTrue( flag ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : flags )
            {
                if ( f.equals( flag ) )
                {
                    return UNSATISFIABLE;
                }
            }
            return new Combined( new String[]{flag}, flags );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : flags )
            {
                if ( f.equals( flag ) )
                {
                    return this;
                }
            }
            String[] flags = Arrays.copyOf( this.flags, this.flags.length + 1 );
            flags[this.flags.length] = flag;
            return new MultipleUnless( flags );
        }
    }

    private static class SingleCombined extends Conditional
    {
        private final String given;
        private final String unless;

        SingleCombined( String given, String unless )
        {
            this.given = given;
            this.unless = unless;
        }

        @Override
        public boolean check( Flags flags )
        {
            return flags.isTrue( given ) && !flags.isTrue( unless );
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( given.equals( flag ) )
            {
                return this;
            }
            if ( unless.equals( flag ) )
            {
                return UNSATISFIABLE;
            }
            return new Combined( new String[]{given, flag}, new String[]{unless} );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            if ( unless.equals( flag ) )
            {
                return this;
            }
            if ( given.equals( flag ) )
            {
                return UNSATISFIABLE;
            }
            return new Combined( new String[]{given}, new String[]{unless, flag} );
        }
    }

    private static class Combined extends Conditional
    {
        private final String[] given;
        private final String[] unless;

        Combined( String[] given, String[] unless )
        {
            this.given = given;
            this.unless = unless;
        }

        @Override
        public boolean check( Flags flags )
        {
            for ( String flag : given )
            {
                if ( !flags.isTrue( flag ) )
                {
                    return false;
                }
            }
            for ( String flag : unless )
            {
                if ( flags.isTrue( flag ) )
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        Conditional given( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : given )
            {
                if ( f.equals( flag ) )
                {
                    return this;
                }
            }
            for ( String f : unless )
            {
                if ( f.equals( flag ) )
                {
                    return UNSATISFIABLE;
                }
            }
            String[] flags = Arrays.copyOf( given, given.length + 1 );
            flags[given.length] = flag;
            return new Combined( flags, unless );
        }

        @Override
        Conditional unless( String flag )
        {
            Objects.requireNonNull( flag, "flag" );
            for ( String f : unless )
            {
                if ( f.equals( flag ) )
                {
                    return this;
                }
            }
            for ( String f : given )
            {
                if ( f.equals( flag ) )
                {
                    return UNSATISFIABLE;
                }
            }
            String[] flags = Arrays.copyOf( unless, unless.length + 1 );
            flags[unless.length] = flag;
            return new Combined( given, flags );
        }
    }
}
