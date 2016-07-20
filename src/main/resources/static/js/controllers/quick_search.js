app.controller('QuickSearchController', function($scope, $state, $rootScope) {
    $rootScope.search ={ text:""};
    $scope.submit = function(){
        if($rootScope.search.text)
            $state.transitionTo('search', {quick:$rootScope.search.text,hideFilter:true});
        else
            $state.transitionTo('search');
    };
});