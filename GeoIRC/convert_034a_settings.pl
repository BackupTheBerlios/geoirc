#!/usr/bin/perl -w

use strict;

sub padding
{
    my( $num_spaces ) = @_;
    
    my $padding = "";
    for( my $i = 0; $i < $num_spaces; $i++ )
    {
	$padding = $padding . "  ";
    }
    
    return $padding;
}

open my $in, "< settings.xml" or die "Failed to open settings.xml.";
open( OUT, "> temp.xml" ) or die "Failed to open temp.xml";

my $line;
my @current_node_name;
my $level = 0;
my $attributes;
my @has_children;
my @printed;
my @closed;
while( <$in> )
{
    $line = $_;
    
    if( $line =~ m/encoding="UTF-8"/ )
    {
	print( OUT $line );
    }
    elsif( $line =~ m/node name="(.+)"/ )
    {
	if( ( $level > 0 ) && ( ! $printed[ $level ] ) )
	{
	    print( OUT padding( $level ) . "<$current_node_name[ $level ]$attributes>\n" );
	    $printed[ $level ] = 1;
	}
	$has_children[ $level ] = 1;
	
	$level++;
	
	my $node_name = $1;
	$node_name =~ tr/ /_/;
	if( $node_name =~ m/^\d/ )
	{
	    $node_name = "_" . $node_name;
	}
	
	$current_node_name[ $level ] = $node_name;
	$attributes = "";
	$printed[ $level ] = 0;
	$closed[ $level ] = 0;
	$has_children[ $level ] = 0;
    }
    elsif( $line =~ m/\/node/ )
    {
	if( ! $has_children[ $level ] )
	{
	    print( OUT padding( $level ) . "<$current_node_name[ $level ]$attributes />\n" );
	    $printed[ $level ] = 1;
	    $closed[ $level ] = 1;
	}
	if( ! $closed[ $level ] )
	{
	    print( OUT padding( $level ) . "</$current_node_name[ $level ]>\n" );
	}
	$level--;
    }
    elsif( $line =~ m/entry key="(.+)" value="(.+)"/ )
    {
	my $key = $1;
	my $value = $2;
	$key =~ tr/ /_/;
	if( $key =~ m/^\d/ )
	{
	    $key = "_" . $key;
	}
	
	$attributes = "$attributes $key=\"$value\"";
    }
}

close( $in );
close( OUT );

# Convert keyboard node (special case).

open $in, "< temp.xml" or die "Failed to open temp.xml.";
open( OUT, "> newsettings.xml" ) or die "Failed to open newsettings.xml";

while( <$in> )
{
    $line = $_;
    
    if( $line =~ m/\s.keyboard / )
    {
	print( OUT "    <keyboard>\n" );
	
	$line =~ s/^    .keyboard //;
	my @keymappings = split(/" /, $line);
	pop( @keymappings );
	my $keystroke;
	my $key;
	my $modifiers;
	my $command;
	foreach my $keymapping ( @keymappings )
	{
	    $keymapping =~ m/(.+)="(.+)/;
	    $keystroke = $1;
	    $command = $2;
	    $keystroke =~ m/(.*)\|(.+)/;
	    $modifiers = $1;
	    $key = uc( $2 );
	    if( $modifiers ne "" )
	    {
		$modifiers =~ s/Alt/alt/;
		$modifiers =~ s/Ctrl/ctrl/;
		$modifiers =~ s/Shift/shift/;
		$modifiers =~ s/Meta/meta/;
		$modifiers = "$modifiers ";
	    }
	    print( OUT "      <keymap keystroke=\"$modifiers$key\" command=\"$command\" />\n" );
	}
	
	print( OUT "    </keyboard>\n" );
    }
    else
    {
	print( OUT $line );
    }
}
close( $in );
close( OUT );