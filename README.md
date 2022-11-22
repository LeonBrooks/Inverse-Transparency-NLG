# Inverse Transparency Natural Language Generation Tool (first prototype)
This tool was developed in conjunction with my paper on the "Representations and Appropriate Simplifications of Data
Accesses With Natural Language Generation" in the context of the 2020 seminar course on Inverse Transparency at the Technical University of Munich. It converts a set of Jira REST API access requests of hypothetical supervisors into natural language summaries for the subordinates (data owners in the context of Inverse Transparency) whose data was accessed.

The complete paper can be found in the `Paper` directory.

##	Input format:
> .txt file with tuples of requesting user and issued request, separated by { character
```sh	
	John{https://jira.atlassian.com/rest/api/latest/search?jql=creator = Peter
```

- the search query must contain spaces between operators and fields
- one request per line
- empty lines may be placed in between requests



##	Output format:
> .txt file



##  Program Parameters:
- path to input file
- path to output file
- the time frame for which this program is run (e.g.: week, 12 days, month, etc.)
- threshold of request issued by a single user which are considered not noteworthy (must be int)
- threshold of request targeting a single user which are considered not noteworthy (must be int)
- [option for detailed output, if wished give "detailed" without quotation marks as last parameter]



The `InOut` directory contains an input and output example
