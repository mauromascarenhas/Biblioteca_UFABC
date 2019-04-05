// Object which stores the last search params
var searchOptions;

// Triggered as soon as DOM elements are available
function documentReady(){
    /*
	 * ORIGINAL CODE HERE...
	 */

    searchOptions = {
        field : 0,
        filters : new Array(searchFilters.length),
        libraries : new Array(searchLibraries.length)
    };

    searchOptions.filters[0] = true;
    for (let i = 1; i < searchOptions.filters.length; ++i)
        searchOptions.filters[i] = false;

    searchOptions.libraries[0] = true;
    for (let i = 1; i < searchOptions.libraries.length; ++i)
        searchOptions.libraries[i] = false;
}

function setSearchFilter(filterStr){
    /*
	 * ORIGINAL CODE HERE...
	 */

    searchOptions = filter;
    return true;
}

// Returns the current search filters (not necessarily applied)
function getSearchFilter(){
    return JSON.stringify(searchOptions);
}