var db = angular.module('db',[]);

db.controller('MainController', function($scope, $http, $window){
    $http.get('admin/db-props')
        .success(function(data){
            $scope.db = data;
        });

    $scope.apply = function(){
        $http.get('admin/change-db?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
            +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
            .success(function(data){
                $scope.success= true;
                $http.get('admin/save-db-props?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
                    +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
                    .success(function(data){
                        $window.location.reload();
                    })
                    .error(function () {
                        $scope.saveFailed= true;
                    });
            })
            .error(function () {
                $scope.failed = true;
            });
    };

    $scope.save = function(){
        $http.get('admin/save-db-props?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
            +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
            .success(function(data){
                $scope.saveSuccess= true;
            })
            .error(function () {
                $scope.saveFailed= true;
            });
    };

    $scope.update = function(){
        $scope.testFailed = false;
        $scope.testSuccess = false;
        $scope.updateFailed = false;
        $scope.updateSuccess = false;
        $http.get('admin/test-db?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
            +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
            .success(function(data){
                $http.get('admin/update-db?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
                    +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
                    .success(function(data){
                        $scope.updateSuccess = true;
                    })
                    .error(function (data) {
                        $scope.errorMessage = data;
                        $scope.updateFailed = true;
                    });
            })
            .error(function () {
                $scope.testFailed = true;
            });
    };

    $scope.test = function(){
        $scope.testFailed = false;
        $scope.testSuccess = false;
        $http.get('admin/test-db?ip='+$scope.db.ip+'&port='+$scope.db.port+'&dbname='
            +$scope.db.dbname+'&username='+$scope.db.username+'&password='+$scope.db.password)
            .success(function(data){
                $scope.testSuccess = true;
            })
            .error(function () {
                $scope.testFailed = true;
            });
    };
});