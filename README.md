								Input format:
.txt file with tuples of requesting user and issued request seperated by { character
	
	John{https://jira.atlassian.com/rest/api/latest/search?jql=creator = Peter

- the search query must contain spaces between operators and fields
- one request per line
- empty lines may be placed inbetween requests



								Output format:
.txt file



							     Program Parameters:
- path to input file
- path to output file
- the time frame for which this program is run (e.g.: week, 12 days, month, etc.)
- threshold of request issued by a single user which are considered not noteworthy (must be int)
- threshold of request targeting a single user which are considered not noteworthy (must be int)
- [option for detailed output, if wished give "detailed" without quotation marks as last parameter]



The folder InOut contains an input and output example