app.controller('PropertiesController', function($scope, $state, $http, $rootScope, $window, $timeout){
    $rootScope.title = "Настройки";
    if($rootScope.authority != "ROLE_ADMIN"){
        $state.transitionTo('home');
    }
    $http.get('admin/properties')
        .success(function (data) {
            $scope.properties = data;
        });

    $scope.save = function(){
        console.log('saving properties');
        $http.post('admin/properties',$scope.properties)
            .success(function (data) {
                $scope.properties = data;
            });
        $window.location.reload();
    };

    $scope.restart = function(){
        $scope.restarting = true;
        $http.post('restart')
            .success(function () {
                $timeout(function () {
                    $scope.restarting = false;
                    $window.location.reload();
                },7000);
            });
    };
});
