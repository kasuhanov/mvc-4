app.controller('NewPatternDialogController', function($scope, $state, $http, $uibModalInstance) {
    $scope.newPattern ={};
    $http.get('category/all')
        .success(function(data) {
            $scope.categories = data;
        });
    $scope.submit = function(){
        $scope.failed = false;
        $scope.success = false;
        $http.post('admin/pattern/'+encodeURIComponent($scope.newPattern.pattern.replace(/\./g, '&#46;')), $scope.newPattern.categories)
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
