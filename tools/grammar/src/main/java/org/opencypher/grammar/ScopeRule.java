package org.opencypher.grammar;

enum ScopeRule
{
    NESTED
            {
                @Override
                <Scope> Scope transform( Scope scope, Transformation<Scope> transformation )
                {
                    return transformation.nestedScope( scope );
                }
            },
    NEW
            {
                @Override
                <Scope> Scope transform( Scope scope, Transformation<Scope> transformation )
                {
                    return transformation.newScope( scope );
                }
            },;

    public interface Transformation<Scope>
    {
        Scope nestedScope( Scope scope );

        Scope newScope( Scope scope );
    }

    abstract <Scope> Scope transform( Scope scope, Transformation<Scope> transformation );
}
