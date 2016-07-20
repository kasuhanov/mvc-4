app.controller('CustomerDialogController', function($scope, $state, $stateParams, $http, $uibModalInstance, paginate) {
    if($stateParams.customerDialog==='false'){
        $uibModalInstance.dismiss();
    }
    init($stateParams.corder,$stateParams.corderby,$stateParams.query);

    function init(order, orderby, query){
        $scope.orderby = orderby===undefined?'id':orderby;
        $scope.order = order===undefined?'desc':order;
        $scope.query = query===undefined?'':query;
        $scope.url='customer/page?query='+$scope.query+'&order='+$scope.order+'&orderby='+$scope.orderby+'&size=';
        $scope.currentPage =  $stateParams.cpage || 1;
        $scope.cpage=true;
        paginate($scope,$http,$state,$stateParams);
    }

    $scope.changeOrder = function(orderby){
        var stateParams = $stateParams;
        stateParams.corderby = orderby;
        if($scope.orderby == orderby){
            if($scope.order == 'asc')
                stateParams.corder = 'desc';
            else
                stateParams.corder = 'asc';
        }else{
            stateParams.corder = 'asc';
        }
        stateParams.cpage = 1;
        init(stateParams.corder, stateParams.corderby,$stateParams.query);
        $state.transitionTo($state.current.name,stateParams,{notify:false});
    };

    $scope.submit = function(customer){
        $uibModalInstance.close(customer);
    };

    $scope.closeDialog = function(){
        $uibModalInstance.dismiss();
    };

    $scope.search = function () {
        $stateParams.query = $scope.query;
        $stateParams.cpage = 1;
        $stateParams.corder = 'desc';
        $stateParams.corderby = 'id';
        init($stateParams.corder, $stateParams.corderby,$stateParams.query);
        $state.transitionTo($state.current.name,$stateParams,{notify:false});
    };

    $scope.clear = function () {
        $scope.query=null;
    };
});
