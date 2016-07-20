app.controller('CategoryController', function($scope, $http, $rootScope) {
    init();
    $rootScope.$watch('completed', function(){
        init();
    });
    function init(){
        $http.get('category/basic-count')
            .success(function(data){
                $scope.counts = data;
            });
        $http.get('category/all')
            //$http.get('category/all?completed='+$rootScope.completed)
            .success(function(data) {
                $scope.categories = data;
            });
    }
    $scope.setCompleted = function(val){
        $rootScope.completed = val;
    }
});