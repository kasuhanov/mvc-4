app.controller('NewCategoryDialogController', function($scope, $state, $http, $uibModalInstance) {
    $scope.submit = function(){
        $scope.failed = false;
        $scope.success = false;
        $http.post('category/', $scope.category)
            .success(function () {
                $scope.failed = false;
                $scope.success = true;
                $uibModalInstance.close();
            })
            .error(function () {
                $scope.failed = true;
                $scope.success = false;
            });
    };
    $scope.closeDialog = function(){
        $uibModalInstance.dismiss();
    };
});
