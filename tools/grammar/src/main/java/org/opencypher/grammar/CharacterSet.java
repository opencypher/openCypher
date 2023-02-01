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

import java.util.Random;

import org.opencypher.tools.io.Output;

import static org.opencypher.grammar.CodePointSet.codePoints;
import static org.opencypher.grammar.CodePointSet.generalCategory;
import static org.opencypher.grammar.CodePointSet.range;
import static org.opencypher.grammar.CodePointSet.single;
import static org.opencypher.grammar.CodePointSet.union;
import static org.opencypher.tools.io.Output.stringBuilder;

public interface CharacterSet
{
    String ANY = "ANY", EOI = "EOI";

    /**
     * Returns the name of this set if this is a named set, or {@code null} if this is not a named set.
     *
     * @return the name of this set.
     */
    String name();

    <EX extends Exception> void accept( DefinitionVisitor<EX> visitor ) throws EX;

    int randomCodePoint( Random random );

    boolean contains( int codePoint );

    boolean hasExclusions();
    
    boolean isControlCharacter();
    
    interface DefinitionVisitor<EX extends Exception>
    {
        default void visitRange( int start, int end ) throws EX
        {
            for ( int cp = start; cp <= end; cp++ )
            {
                visitCodePoint( cp );
            }
        }

        void visitCodePoint( int cp ) throws EX;

        interface NamedSetVisitor<EX extends Exception> extends DefinitionVisitor<EX>
        {
            ExclusionVisitor<EX> visitSet( String name ) throws EX;

        }
    }

    interface ExclusionVisitor<EX extends Exception> extends AutoCloseable
    {
        void excludeCodePoint( int cp ) throws EX;

        default void excludeRange( int start, int end ) throws EX
        {
            for ( int cp = start; cp <= end; cp++ )
            {
                excludeCodePoint( cp );
            }
        }

        void excludeSet( String name ) throws EX;

        @Override
        default void close() throws EX
        {
        }
    }

    enum Unicode
    {
        /* Any character */
        ANY( CodePointSet.ANY ),

        // Named Control Characters
        /** ASCII control character {@code NUL} ("null"). */
        NUL( single( 0x00 ) ),
        /** ASCII control character {@code SOH} ("start of heading"). */
        SOH( single( 0x01 ) ),
        /** ASCII control character {@code STX} ("start of text"). */
        STX( single( 0x02 ) ),
        /** ASCII control character {@code ETX} ("end of text"). */
        ETX( single( 0x03 ) ),
        /** ASCII control character {@code EOT} ("end of transmission"). */
        EOT( single( 0x04 ) ),
        /** ASCII control character {@code ENQ} ("enquiry"). */
        ENQ( single( 0x05 ) ),
        /** ASCII control character {@code ACK} ("ack"). */
        ACK( single( 0x06 ) ),
        /** ASCII control character {@code BEL} ("bell"). */
        BEL( single( 0x07 ) ),
        /** ASCII control character {@code BS} ("backspace"). */
        BS( single( 0x08 ) ),
        /** ASCII control character {@code TAB} ("horizontal tab"). */
        TAB( single( 0x09 ) ),
        /** ASCII control character {@code LF} ("line feed"). */
        LF( single( 0x0A ) ),
        /** ASCII control character {@code VT} ("vertical tab"). */
        VT( single( 0x0B ) ),
        /** ASCII control character {@code FF} ("form feed"). */
        FF( single( 0x0C ) ),
        /** ASCII control character {@code CR} ("carriage return"). */
        CR( single( 0x0D ) ),
        /** ASCII control character {@code SO} ("shift out"). */
        SO( single( 0x0E ) ),
        /** ASCII control character {@code SI} ("shift in"). */
        SI( single( 0x0F ) ),
        /** ASCII control character {@code DLE} ("data link escape"). */
        DLE( single( 0x10 ) ),
        /** ASCII control character {@code DC1} ("device control 1 - XON"). */
        DC1( single( 0x11 ) ),
        /** ASCII control character {@code DC2} ("device control 2"). */
        DC2( single( 0x12 ) ),
        /** ASCII control character {@code DC3} ("device control 3 - XOFF"). */
        DC3( single( 0x13 ) ),
        /** ASCII control character {@code DC4} ("device control 4"). */
        DC4( single( 0x14 ) ),
        /** ASCII control character {@code NAK} ("negative ack"). */
        NAK( single( 0x15 ) ),
        /** ASCII control character {@code SYN} ("synchronous idle"). */
        SYN( single( 0x16 ) ),
        /** ASCII control character {@code ETB} ("end of transmission block"). */
        ETB( single( 0x17 ) ),
        /** ASCII control character {@code CAN} ("cancel"). */
        CAN( single( 0x18 ) ),
        /** ASCII control character {@code EM} ("end of medium"). */
        EM( single( 0x19 ) ),
        /** ASCII control character {@code SUB} ("substitute"). */
        SUB( single( 0x1A ) ),
        /** ASCII control character {@code ESC} ("escape"). */
        ESC( single( 0x1B ) ),
        /** ASCII control character {@code FS} ("file separator"). */
        FS( single( 0x1C ) ),
        /** ASCII control character {@code GS} ("group separator"). */
        GS( single( 0x1D ) ),
        /** ASCII control character {@code RS} ("record separator"). */
        RS( single( 0x1E ) ),
        /** ASCII control character {@code US} ("unit separator"). */
        US( single( 0x1F ) ),
        /** ASCII space character. */
        SPACE( single( 0x20 ) ),
        /** ASCII control character {code DEL}. */
        DEL( single( 0x7F ) ),

        // General Categories

        /** Other, Control */
        Cc( generalCategory( Character.CONTROL ) ),
        /** Other, Format */
        Cf( generalCategory( Character.FORMAT ) ),
        /** Other, Not Assigned (no characters have this property) */
        Cn( generalCategory( Character.UNASSIGNED ) ),
        /** Other, Private Use */
        Co( generalCategory( Character.PRIVATE_USE ) ),
        /** Other, Surrogate */
        Cs( generalCategory( Character.SURROGATE ) ),

        /** Letter, Lowercase */
        Ll( generalCategory( Character.LOWERCASE_LETTER ) ),
        /** Letter, Modifier */
        Lm( generalCategory( Character.MODIFIER_LETTER ) ),
        /** Letter, Other */
        Lo( generalCategory( Character.OTHER_LETTER ) ),
        /** Letter, Titlecase */
        Lt( generalCategory( Character.TITLECASE_LETTER ) ),
        /** Letter, Uppercase */
        Lu( generalCategory( Character.UPPERCASE_LETTER ) ),

        /** Mark, Spacing Combining */
        Mc( generalCategory( Character.COMBINING_SPACING_MARK ) ),
        /** Mark, Enclosing */
        Me( generalCategory( Character.ENCLOSING_MARK ) ),
        /** Mark, Nonspacing */
        Mn( generalCategory( Character.NON_SPACING_MARK ) ),

        /** Number, Decimal Digit */
        Nd( generalCategory( Character.DECIMAL_DIGIT_NUMBER ) ),
        /** Number, Letter */
        Nl( generalCategory( Character.LETTER_NUMBER ) ),
        /** Number, Other */
        No( generalCategory( Character.OTHER_NUMBER ) ),

        /** Punctuation, Connector */
        Pc( generalCategory( Character.CONNECTOR_PUNCTUATION ) ),
        /** Punctuation, Dash */
        Pd( generalCategory( Character.DASH_PUNCTUATION ) ),
        /** Punctuation, Close */
        Pe( generalCategory( Character.END_PUNCTUATION ) ),
        /** Punctuation, Final quote (may behave like 'Ps' or 'Pe' depending on usage) */
        Pf( generalCategory( Character.FINAL_QUOTE_PUNCTUATION ) ),
        /** Punctuation, Initial quote (may behave like 'Ps' or 'Pe' depending on usage) */
        Pi( generalCategory( Character.INITIAL_QUOTE_PUNCTUATION ) ),
        /** Punctuation, Other */
        Po( generalCategory( Character.OTHER_PUNCTUATION ) ),
        /** Punctuation, Open */
        Ps( generalCategory( Character.START_PUNCTUATION ) ),

        /** Symbol, Currency */
        Sc( generalCategory( Character.CURRENCY_SYMBOL ) ),
        /** Symbol, Modifier */
        Sk( generalCategory( Character.MODIFIER_SYMBOL ) ),
        /** Symbol, Math */
        Sm( generalCategory( Character.MATH_SYMBOL ) ),
        /** Symbol, Other */
        So( generalCategory( Character.OTHER_SYMBOL ) ),

        /** Separator, Line */
        Zl( generalCategory( Character.LINE_SEPARATOR ) ),
        /** Separator, Paragraph */
        Zp( generalCategory( Character.PARAGRAPH_SEPARATOR ) ),
        /** Separator, Space */
        Zs( generalCategory( Character.SPACE_SEPARATOR ) ),

        White_Space( codePoints(
                '\t', '\n', '\u000B', '\f', '\r', ' ',
                /* extra in Java
                '\u001C', // FS - File Separator
                '\u001D', // GS - Group Separator
                '\u001E', // RS - Record Separator
                '\u001F', // US - Unit Separator
                //*/
                '\u0085', // NEXT LINE (NEL)            -- not in Java
                '\u00A0', // NO-BREAK SPACE             -- not in Java
                '\u1680', // OGHAM SPACE MARK
                /* extra in Java
                '\u180E', // MONGOLIAN VOWEL SEPARATOR
                //*/
                '\u2000', // EN QUAD
                '\u2001', // EM QUAD
                '\u2002', // EN SPACE
                '\u2003', // EM SPACE
                '\u2004', // THREE-PER-EM SPACE
                '\u2005', // FOUR-PER-EM SPACE
                '\u2006', // SIX-PER-EM SPACE
                '\u2007', // FIGURE SPACE               -- not in Java
                '\u2008', // PUNCTUATION SPACE
                '\u2009', // THIN SPACE
                '\u200A', // HAIR SPACE
                '\u2028', // LINE SEPARATOR
                '\u2029', // PARAGRAPH SEPARATOR
                '\u202F', // NARROW NO-BREAK SPACE      -- not in Java
                '\u205F', // MEDIUM MATHEMATICAL SPACE
                '\u3000'  // IDEOGRAPHIC SPACE
        ) ),
        Pattern_White_Space( codePoints(
                // '\u001C' // FS - File Separator
                // '\u001D' // GS - Group Separator
                // '\u001E' // RS - Record Separator
                // '\u001F' // US - Unit Separator
                '\t', '\n', '\u000B', '\f', '\r', ' ',
                '\u0085', // NEXT LINE (NEL)
                '\u200E', // LEFT-TO-RIGHT MARK
                '\u200F', // RIGHT-TO-LEFT MARK
                '\u2028', // LINE SEPARATOR
                '\u2029'  // PARAGRAPH SEPARATOR
        ) ),
        Pattern_Syntax( union(
                range( '!', '/' ), range( ':', '@' ), range( '['/*,'\',']'*/, '^' ), codePoints( '`' ),
                range( '{',/*,'|','}'*/'~' ), range( '\u00A1', '\u00A7' ), codePoints(
                        '\u00A9', /* COPYRIGHT SIGN */
                        '\u00AB', /* LEFT-POINTING DOUBLE ANGLE QUOTATION MARK */
                        '\u00AC', /* NOT SIGN */
                        '\u00AE', /* REGISTERED SIGN */
                        '\u00B0', /* DEGREE SIGN */
                        '\u00B1', /* PLUS-MINUS SIGN */
                        '\u00B6', /* PILCROW SIGN */
                        '\u00BB', /* RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK */
                        '\u00BF', /* INVERTED QUESTION MARK */
                        '\u00D7', /* MULTIPLICATION SIGN */
                        '\u00F7'  /* DIVISION SIGN */ ),
                range( '\u2010', '\u2027' ), range( '\u2030', '\u203E' ), range( '\u2041', '\u2053' ),
                range( '\u2055', '\u205E' ), range( '\u2190', '\u245F' ), range( '\u2500', '\u2775' ),
                range( '\u2794', '\u2BFF' ), range( '\u2E00', '\u2E7F' ),
                codePoints( '\u3001', '\u3002', '\u3003' ), range( '\u3008', '\u3020' ),
                codePoints( '\u3030', '\uFD3E', '\uFD3F', '\uFE45', '\uFE46' ) ) ),

        // Annex 31

        Other_ID_Start( codePoints( '\u2118', '\u212E', '\u309B', '\u309C' ) ),
        Other_ID_Continue( codePoints(
                '\u00B7', '\u0387', '\u1369', '\u136A', '\u136B', '\u136C',
                '\u136D', '\u136E', '\u136F', '\u1370', '\u1371', '\u19DA' ) ),
        /** Characters allowed as initial character of an identifier. http://unicode.org/reports/tr31/ */
        ID_Start( union( Ll.set, Lm.set, Lo.set, Lt.set, Lu.set, Nl.set, Other_ID_Start.set )
                          .except( Pattern_Syntax.set ).except( Pattern_White_Space.set ) ),
        /** Characters allowed in an identifier. http://unicode.org/reports/tr31/ */
        ID_Continue( union( ID_Start.set, Mn.set, Mc.set, Nd.set, Pc.set, Other_ID_Continue.set )
                             .except( Pattern_Syntax.set ).except( Pattern_White_Space.set ) ),;

        final CodePointSet set;

        private static final String[] CONTROL_CHAR_NAMES = {
                NUL.name(), SOH.name(), STX.name(), ETX.name(), EOT.name(), ENQ.name(), ACK.name(), BEL.name(),
                BS.name(), TAB.name(), LF.name(), VT.name(), FF.name(), CR.name(), SO.name(), SI.name(),
                DLE.name(), DC1.name(), DC2.name(), DC3.name(), DC4.name(), NAK.name(), SYN.name(), ETB.name(),
                CAN.name(), EM.name(), SUB.name(), ESC.name(), FS.name(), GS.name(), RS.name(), US.name()};

        
        Unicode( CodePointSet set )
        {
            (this.set = set).name = name();
        }

        /**
         * Returns the unicode character set representing the general category of the given code point.
         *
         * @param cp the code point to get the general category of.
         * @return The general category of the given code point.
         * @see #Cc Cc - Other, Control
         * @see #Cf Cf - Other, Format
         * @see #Cn Cn - Other, Not Assigned
         * @see #Co Co - Other, Private Use
         * @see #Cs Cs - Other, Surrogate
         * @see #Ll Ll - Letter, Lowercase
         * @see #Lm Lm - Letter, Modifier
         * @see #Lo Lo - Letter, Other
         * @see #Lt Lt - Letter, Titlecase
         * @see #Lu Lu - Letter, Uppercase
         * @see #Mc Mc - Mark, Spacing Combining
         * @see #Me Me - Mark, Enclosing
         * @see #Mn Mn - Mark, Nonspacing
         * @see #Nd Nd - Number, Decimal Digit
         * @see #Nl Nl - Number, Letter
         * @see #No No - Number, Other
         * @see #Pc Pc - Punctuation, Connector
         * @see #Pd Pd - Punctuation, Dash
         * @see #Pe Pe - Punctuation, Close
         * @see #Pf Pf - Punctuation, Final quote
         * @see #Pi Pi - Punctuation, Initial quote
         * @see #Po Po - Punctuation, Other
         * @see #Ps Ps - Punctuation, Open
         * @see #Sc Sc - Symbol, Currency
         * @see #Sk Sk - Symbol, Modifier
         * @see #Sm Sm - Symbol, Math
         * @see #So So - Symbol, Other
         * @see #Zl Zl - Separator, Line
         * @see #Zp Zp - Separator, Paragraph
         * @see #Zs Zs - Separator, Space
         */
        public static Unicode getGeneralCategory( int cp )
        {
            return fromCharacterType( Character.getType( cp ) );
        }

        /**
         * Returns the unicode character set representing the general category for the corresponding
         * {@linkplain Character#getType(char) numeric java value of the category}.
         *
         * @param type the {@linkplain Character#getType(char) numeric java value of the general category}.
         * @return The corresponding general category.
         * @see #getGeneralCategory(int)
         */
        public static Unicode fromCharacterType( int type )
        {
            switch ( type )
            { // <pre>
            case Character.UNASSIGNED:                return Cn;
            case Character.UPPERCASE_LETTER:          return Lu;
            case Character.LOWERCASE_LETTER:          return Ll;
            case Character.TITLECASE_LETTER:          return Lt;
            case Character.MODIFIER_LETTER:           return Lm;
            case Character.OTHER_LETTER:              return Lo;
            case Character.NON_SPACING_MARK:          return Mn;
            case Character.ENCLOSING_MARK:            return Me;
            case Character.COMBINING_SPACING_MARK:    return Mc;
            case Character.DECIMAL_DIGIT_NUMBER:      return Nd;
            case Character.LETTER_NUMBER:             return Nl;
            case Character.OTHER_NUMBER:              return No;
            case Character.SPACE_SEPARATOR:           return Zs;
            case Character.LINE_SEPARATOR:            return Zl;
            case Character.PARAGRAPH_SEPARATOR:       return Zp;
            case Character.CONTROL:                   return Cc;
            case Character.FORMAT:                    return Cf;
            case Character.PRIVATE_USE:               return Co;
            case Character.SURROGATE:                 return Cs;
            case Character.DASH_PUNCTUATION:          return Pd;
            case Character.START_PUNCTUATION:         return Ps;
            case Character.END_PUNCTUATION:           return Pe;
            case Character.CONNECTOR_PUNCTUATION:     return Pc;
            case Character.OTHER_PUNCTUATION:         return Po;
            case Character.MATH_SYMBOL:               return Sm;
            case Character.CURRENCY_SYMBOL:           return Sc;
            case Character.MODIFIER_SYMBOL:           return Sk;
            case Character.OTHER_SYMBOL:              return So;
            case Character.INITIAL_QUOTE_PUNCTUATION: return Pi;
            case Character.FINAL_QUOTE_PUNCTUATION:   return Pf;
            } //</pre>
            throw new IllegalArgumentException( "Unknown character type: " + type );
        }

        public static String toSetString( CharacterSet characters )
        {
            Output.Readable result = stringBuilder();
            characters.accept( new DefinitionVisitor.NamedSetVisitor<RuntimeException>()
            {
                @Override
                public ExclusionVisitor<RuntimeException> visitSet( String base )
                {
                    return new ExclusionVisitor<RuntimeException>()
                    {
                        boolean inSet;

                        @Override
                        public void excludeRange( int start, int end )
                        {
                            if ( (end - start) == 1 )
                            {
                                excludeCodePoint( start );
                                excludeCodePoint( end );
                            }
                            else
                            {
                                init( true );
                                append( start );
                                result.append( '-' );
                                append( end );
                            }
                        }

                        @Override
                        public void excludeCodePoint( int cp )
                        {
                            init( true );
                            append( cp );
                        }

                        @Override
                        public void excludeSet( String name )
                        {
                            init( false );
                            result.append( '-' );
                            namedSet( name );
                        }

                        void init( boolean openSet )
                        {
                            if ( result.length() == 0 )
                            {
                                result.append('[');
                                namedSet( base );
                            }
                            if ( openSet )
                            {
                                if ( !inSet )
                                {
                                    result.append( "-[" );
                                    inSet = true;
                                }
                            }
                            else
                            {
                                if ( inSet )
                                {
                                    result.append( ']' );
                                    inSet = false;
                                }
                            }
                        }

                        @Override
                        public void close()
                        {
                            if ( result.length() == 0 )
                            {
                                namedSet( base );
                            }
                            else if ( inSet )
                            {
                                result.append( ']' );
                            }
                        }
                    };
                }

                @Override
                public void visitRange( int start, int end )
                {
                    if ( (end - start) == 1 )
                    {
                        visitCodePoint( start );
                        visitCodePoint( end );
                    }
                    else
                    {
                        init();
                        append( start );
                        result.append( '-' );
                        append( end );
                    }
                }

                @Override
                public void visitCodePoint( int cp )
                {
                    init();
                    append( cp );
                }

                void init()
                {
                    if ( result.length() == 0 )
                    {
                        result.append( '[' );
                    }
                }

                void append( int cp )
                {
                    result.append( escapeCodePoint( cp ) );
                }

                void namedSet( String name )
                {
                    try
                    {
                        Unicode.valueOf( name ).set.setName( result );
                    }
                    catch ( Exception ignored )
                    {
                        result.append( "[:" ).append( name ).append( ":" );
                    }
                }
            } );
            return result.append( ']' ).toString();
        }
        
        /**
         * antlr4 4.7.2 recognises standard set names, but not the control chars
         */
        public boolean isSingleCharacter()
        {
            return set.isSingleCharacter();
        }
    }

    public static String controlCharName( int cp )
    {
        if ( cp < Unicode.CONTROL_CHAR_NAMES.length )
        {
            return Unicode.CONTROL_CHAR_NAMES[cp];
        }
        else if ( cp == 0x7f )
        {
            return Unicode.DEL.name();
        }
        return null;
    }
    
    static String escapeCodePoint( int cp )
    {
        return String.format( cp > 0xFFFF ? "\\U%08X" : "\\u%04X", cp );
    }
}
