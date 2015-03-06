#!/usr/bin/perl                                                                               
use strict;
use warnings;
use Switch;
use Getopt::Long qw(:config no_ignore_case);

#constants
our $OUTPUT_DIR="/tmp/rpc-output/";

#INFO ABOUT STRUCTURE
#     %MANAGERS
#         |
#   {manager name}    
#         |
#     @METHODS
#         |
#       {0..x}
#         |
#      %METHOD
#       |    |
#   {"name"}{javadocs}
#     |          |
# $methodName  @javadocs
#                |
#              {0..x}
#                |
#              %javadoc
#              | | | |
#   {"text","params","throws","return"}
#      |       |       |         |
#   @text    @params  @throws   $return
#      |       |       |         
#   {0..x}   {0..x}  {0..x}   
#      |       |       |
#    $text  $param    $throw

#variables
my $managers = {};

# SUB HELP
# help info
sub help {
	return qq{
  Generate html javadoc from perun. 
  Path to directory with methods required.
  ----------------------------------------
  Available options:
  --defaultPathDir  | -d path to directory with rpc methods
  --help            | -h prints this help
	};
}

# SUB PROCESSFILE
# process every file in directory
sub processFile {
	my $file_name = $_[0];
	my $dir_path = $_[1];
	my $fullPath = $dir_path . "/" . $file_name;
	my $managerName;
	if($file_name =~ m/^(.*)Method\.java/) {
		$managerName = "$1";
	} elsif ($file_name =~ m/^(.*)\..*/) {
		$managerName = "$1";
	} else {
		$managerName = $file_name;
	}

	# open file
	print "RESIM: " . $fullPath . " a manager " . $managerName . " \n";
	open my $handler, $fullPath or die "Could not open $fullPath";

	# phase of looking for method
	# 0 - looking for start of javadoc symbols /*# (if found -> 1)
	# 1 - looking for parts of one javadoc or end of this javadoc (if end found -> 2)
	# 2 - looking for another javadoc (if found -> 1) or name of method (if found -> 0)
	my $phase=0; #phase of looking in file
	my @methods = ();
	my $method = {};
	my @params = ();
	my @textLines = ();
	my @throws = ();
	my $return;
	my @javadocs = (); #array with javadocs of one method
	while (my $line = <$handler>) {
		# skip every line which start // (one line comment)
		next if($line =~ m/^\s*\/\/.*/);

		# skip all comments which start /* without hash
		# !!! THIS IS NOT IMPLEMENTED, IF THERE IS SOME /* COMMENT ON IMPORTANT PLACE
		# IT CAN CREATE BAD DOCUMENTATION, NEED TO SOLVE OR DO NOT USE THIS TYPE OF COMMENTS

		switch ($phase) {
			case 0 {
				if($line =~ m/^\s*\/\*\#/) { $phase=1; }
			}
			case 1 {
				if($line =~ m/^\s*[*]\s*\@param\s*(.*)/) {
					push @params, $1;
				} elsif($line =~ m/^\s*\*\s*[@]return\s*(.*)/) {
					$return="<$1>";
				} elsif($line =~ m/^\s*\*\s*[@]throw\s*(.*)/) { 
					push @throws, $1;
				} elsif($line =~ m/^\s*\*\//) { 
					$phase=2;
					# local variables for puprose of saving information
					my $javadoc={};
					my @localParams = @params;
					my @localThrows = @throws;
					my @localTextLines = @textLines;
					# save one javadoc
					$javadoc->{'params'} = \@localParams;
					$javadoc->{'throws'}= \@localThrows;
					$javadoc->{'return'}= $return;
					$javadoc->{'text'}= \@localTextLines;
					push @javadocs, $javadoc;
					#reset all needed variables
					@params=();
					@textLines=();
					@throws=();
					undef $return;
					$javadoc=();
				} elsif($line =~ m/^\s*\*\s*(.*)/) {
					push @textLines, $1;				
				} else {
					#skip this line, it is probably space or something nasty, we dont need it
				}
			}
			case 2 {
				if($line =~ m/^\s*\/[*]\#/) { 
					$phase=1; 
				} elsif($line =~ m/^\s*([a-zA-Z0-9]+)\s*\{.*/) {
					$phase=0;
					$method->{'name'}=$1;
					#local variable for saving all javadocs
					my @localJavadocs = @javadocs;
					$method->{'javadocs'}= \@localJavadocs;
					#local variable for saving one method
					my $localMethod = $method;
					push @methods, $localMethod;	
					#reset all needed variables
					@javadocs = ();
					$method = {};
				} else {
					#skip this line, it is probably some code or empty line, we dont need it
				}
			}
		}		
	}
	if($phase != 0) {
		die "Some phase was not ended correctly for file $file_name and phase $phase!";
	}

	#save all parsed methods
	$managers->{$managerName}=\@methods;
	
	close($handler);
}

#START OF MAIN PROGRAM

# default options
my $defaultPathDir;
GetOptions ("help|h" => sub {print help(); exit 0;},
						"defaultPathDir|d=s" => \$defaultPathDir) || die help();

#if directory with methods is not set
unless (defined($defaultPathDir)) { die "ERROR: default path to directory where methods exists is required \n";}

#if directory with methods not exists
unless (-d $defaultPathDir) { die "ERROR: directory " . $defaultPathDir . " not exists!";}

#open input dir
opendir (DIR, $defaultPathDir) or die "Cannot open directory with files (with methods)!";

#create output dir if not exists yet
unless (-d $OUTPUT_DIR) {
	mkdir $OUTPUT_DIR;
	print $OUTPUT_DIR . " was created. \n";
}

#process all files in dir
while (my $file = readdir(DIR)) {
	next if ($file =~ m/^\./);
	processFile($file, $defaultPathDir)
}

#separate printing data from processing data
print "\n --------------------------------------------- \n\n";
#print all data
foreach my $manager (sort(keys %{$managers})) {
	#get managers keys
	print $manager . "\n";
	
	my $methods = $managers->{$manager};
	my $sortedMethods={};
	
	#prepare sorted methods
	foreach my $notSortedMethod (@{$methods}) {
		#get names of methods
		my $methodName = $notSortedMethod->{'name'};
		my $javadocs = $notSortedMethod->{'javadocs'};
		$sortedMethods->{$methodName}=$notSortedMethod->{'javadocs'};
	}

	#print sorted methods
	foreach	my $sortedMethod (sort(keys %{$sortedMethods})) {
		my $javadocs = $sortedMethods->{$sortedMethod};
		print "\t" . $sortedMethod . "\n";
		#print info about javadocs
		foreach my $javadoc (@{$javadocs}) {
			my $throws = $javadoc->{'throws'};
			my $return = $javadoc->{'return'};
			my $params = $javadoc->{'params'};
			my $texts = $javadoc->{'text'};

			#print text
			foreach my $text (@{$texts}) {
				print "\t\tTEXT:" . $text . "\n";
			}

			#print params
			foreach my $param (@{$params}) {
				print "\t\tPARAM:" . $param . "\n";
			}

			#print throw
			foreach my $throw (@{$throws}) {
				print "\t\tTHROW:" . $throw . "\n";
			}

			#print return
			if(defined $return) {
				print "\t\tRETURN:" . $return . "\n";
			} else {
				print "\t\tRETURN: void\n";
			}
		}
	}
}

#END OF MAIN PROGRAM

#closing DIR
closedir(DIR);
exit 0;
