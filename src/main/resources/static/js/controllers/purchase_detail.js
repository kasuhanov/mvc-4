app.controller('PurchaseDetailController', function($scope, $http, $state, $stateParams, $rootScope, $uibModal) {
    $rootScope.title = "Закупка";
    init();
    $rootScope.$watch('authenticated', function(){
        init();
    });
    $scope.addToFavorites = function(){
        $http.get('fav/add/purchase/'+$stateParams.id)
            .success(function() {
                $scope.fav = !$scope.fav;
            });
    };
    function init(){
        if($rootScope.authenticated&&$rootScope.authority =='ROLE_USER'){
            $http.get('purchase/for-user/'+$stateParams.id)
                .success(function(data) {
                    $scope.purchase = data.purchase;
                    $scope.fav = data.fav;
                    $rootScope.title = "Закупка №"+$scope.purchase.id;
                    if($scope.purchase.type!='EP'){
                        $scope.order = true;
                    } else{
                        $scope.lots = true;
                    }
                })
                .error(function(code,status){
                    var stateParams = {};
                    stateParams.code = status;
                    if(status==404){
                        stateParams.message = 'Закупка № '+$stateParams.id+' не найдена.';
                    }

                    if(status==500){
                        stateParams.message = 'При обработке запроса на сервере произошла ошибка.';
                    }
                    $state.transitionTo('error',stateParams);
                });
        } else {
            $http.get('purchase/'+$stateParams.id)
                .success(function(data) {
                    $scope.purchase = data;
                    $rootScope.title = "Закупка №"+$scope.purchase.id;
                    if($scope.purchase.type!='EP'){
                        $scope.order = true;
                    } else{
                        $scope.lots = true;
                    }
                })
                .error(function(code,status){
                    var stateParams = {};
                    stateParams.code = status;
                    if(status==404){
                        stateParams.message = 'Закупка № '+$stateParams.id+' не найдена.';
                    }

                    if(status==500){
                        stateParams.message = 'При обработке запроса на сервере произошла ошибка.';
                    }
                    $state.transitionTo('error',stateParams);
                });
        }
        $http.get('category/purchase/'+$stateParams.id)
            .success(function(data) {
                $scope.categories = data;
            });
    }
    $scope.open = function (okpd) {
        var modalInstance = $uibModal.open({
            templateUrl: '../pages/okpd_dialog.html',
            controller: 'OkpdDialogController',
            size: 'lg',
            resolve: {
                okpd: function () {
                    return okpd;
                }
            }
        });
    };
    $scope.detailItemClick = function(item){
        if(item == 'order'){
            $scope.order = true;
            $scope.documentation = false;
            $scope.lots = false;
            $scope.contact = false;
        }
        if(item == 'documentation'){
            $scope.order = false;
            $scope.documentation = true;
            $scope.lots = false;
            $scope.contact = false;
        }
        if(item == 'lots'){
            $scope.order = false;
            $scope.documentation = false;
            $scope.lots = true;
            $scope.contact = false;
        }
        if(item == 'contact'){
            $scope.order = false;
            $scope.documentation = false;
            $scope.lots = false;
            $scope.contact = true;
        }
    }
});