/*
 * Copyright (c) 2015-2018 "Neo Technology,"
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
 */
package org.opencypher.tools.io;

import java.util.Formatter;
import java.util.Locale;

abstract class FormattingOutput<Target> extends BaseOutput<Target>
{
    FormattingOutput( Target output )
    {
        super( output );
    }

    private Formatter formatter;

    // FORMAT

    @Override
    public final Output format( String format, Object... args )
    {
        if ( formatter == null || formatter.locale() != Locale.getDefault() )
        {
            formatter = new Formatter( this );
        }
        formatter.format( Locale.getDefault(), format, args );
        return this;
    }

    @Override
    public final Output format( Locale l, String format, Object... args )
    {
        if ( formatter == null || formatter.locale() != l )
        {
            formatter = new Formatter( this, l );
        }
        formatter.format( l, format, args );
        return this;
    }
}
